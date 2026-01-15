/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.incident;

import io.flowset.control.dto.ActivityIncidentData;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.HistoricIncidentDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeIncidentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7IncidentServiceTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    IncidentService incidentService;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Load activity incidents statistics for existing process instance")
    void givenExistingProcessInstance_whenFindRuntimeIncidents_thenAllIncidentsReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testMultipleFailedJobIncidents.bpmn")
                .startByKey("testMultipleFailedJobIncidents")
                .waitJobsExecution();

        String instanceId = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "testMultipleFailedJobIncidents").get(0);

        //when+
        List<ActivityIncidentData> activityIncidents = incidentService.findRuntimeIncidents(instanceId);

        //then
        assertThat(activityIncidents)
                .isNotNull()
                .hasSize(2)
                .extracting(ActivityIncidentData::getElementId, ActivityIncidentData::getIncidentCount)
                .contains(tuple("throwOneExceptionTask", 1),
                        tuple("throwsMultipleExceptionsTask", 3));
    }

    @Test
    @DisplayName("Find existing runtime incident by id")
    void givenActiveExistingRuntimeIncident_whenFindRuntimeIncidentById_thenIncidentReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "testFailedJobIncident").get(0);

        RuntimeIncidentDto sourceIncident = camundaRestTestHelper.findRuntimeIncidentsByInstanceId(camunda7, instanceId).get(0);

        //when
        IncidentData foundIncident = incidentService.findRuntimeIncidentById(sourceIncident.getId());

        //then
        assertThat(foundIncident).isNotNull();
        assertThat(foundIncident.getIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getActivityId()).isEqualTo("throwsExceptionTask");
        assertThat(foundIncident.getFailedActivityId()).isEqualTo("throwsExceptionTask");
        assertThat(foundIncident.getCauseIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getRootCauseIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getMessage()).isNotNull()
                .contains("Some service not available")
                .isEqualTo(sourceIncident.getIncidentMessage());
        assertThat(foundIncident.getTimestamp()).isNotNull();
        assertThat(foundIncident.getProcessInstanceId()).isEqualTo(instanceId);
        assertThat(foundIncident.getProcessDefinitionId()).isEqualTo(sourceIncident.getProcessDefinitionId());
        assertThat(foundIncident.getType()).isEqualTo("failedJob");
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find runtime incident by id if engine is not available")
    void givenExistingRuntimeIncidentAndNotAvailableEngine_whenFindRuntimeIncidentById_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "testFailedJobIncident").get(0);

        RuntimeIncidentDto sourceIncident = camundaRestTestHelper.findRuntimeIncidentsByInstanceId(camunda7, instanceId).get(0);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> incidentService.findRuntimeIncidentById(sourceIncident.getId()))
                .isInstanceOf(EngineConnectionFailedException.class);
    }


    @Test
    @DisplayName("Find historic resolved incident by id")
    void givenExistingResolvedHistoricIncident_whenFindHistoricIncidentById_thenIncidentReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs()
                .waitJobsExecution();

        String instanceId = camundaRestTestHelper.getHistoricInstancesByKey(camunda7, "testResolvedFailedJobIncident").get(0);
        HistoricIncidentDto sourceIncident = camundaRestTestHelper.findHistoricIncidentsByInstanceId(camunda7, instanceId).get(0);

        //when
        HistoricIncidentData foundIncident = incidentService.findHistoricIncidentById(sourceIncident.getId());

        //then
        assertThat(foundIncident).isNotNull();
        assertThat(foundIncident.getIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getActivityId()).isEqualTo("throwsExceptionConditionallyTask");
        assertThat(foundIncident.getResolved()).isTrue();
        assertThat(foundIncident.getCauseIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getRootCauseIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getMessage()).isNotNull()
                .contains("Some service not available")
                .isEqualTo(sourceIncident.getIncidentMessage());
        assertThat(foundIncident.getDeleted()).isFalse();
        assertThat(foundIncident.getCreateTime()).isNotNull();
        assertThat(foundIncident.getOpen()).isFalse();
        assertThat(foundIncident.getProcessDefinitionId()).isEqualTo(sourceIncident.getProcessDefinitionId());
        assertThat(foundIncident.getProcessDefinitionKey()).isEqualTo("testResolvedFailedJobIncident");
        assertThat(foundIncident.getEndTime()).isNotNull();
        assertThat(foundIncident.getProcessInstanceId()).isEqualTo(instanceId);
        assertThat(foundIncident.getType()).isEqualTo("failedJob");
    }

    @Test
    @DisplayName("Find historic open incident by id")
    void givenExistingOpenHistoricIncident_whenFindHistoricIncidentById_thenIncidentReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = camundaRestTestHelper.getHistoricInstancesByKey(camunda7, "testFailedJobIncident").get(0);
        HistoricIncidentDto sourceIncident = camundaRestTestHelper.findHistoricIncidentsByInstanceId(camunda7, instanceId).get(0);

        //when
        HistoricIncidentData foundIncident = incidentService.findHistoricIncidentById(sourceIncident.getId());

        //then
        assertThat(foundIncident).isNotNull();
        assertThat(foundIncident.getIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getActivityId()).isEqualTo("throwsExceptionTask");
        assertThat(foundIncident.getResolved()).isFalse();
        assertThat(foundIncident.getCauseIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getRootCauseIncidentId()).isEqualTo(sourceIncident.getId());
        assertThat(foundIncident.getMessage()).isNotNull()
                .contains("Some service not available")
                .isEqualTo(sourceIncident.getIncidentMessage());
        assertThat(foundIncident.getDeleted()).isFalse();
        assertThat(foundIncident.getCreateTime()).isNotNull();
        assertThat(foundIncident.getOpen()).isTrue();
        assertThat(foundIncident.getProcessDefinitionId()).isEqualTo(sourceIncident.getProcessDefinitionId());
        assertThat(foundIncident.getProcessDefinitionKey()).isEqualTo("testFailedJobIncident");
        assertThat(foundIncident.getEndTime()).isNull();
        assertThat(foundIncident.getProcessInstanceId()).isEqualTo(instanceId);
        assertThat(foundIncident.getType()).isEqualTo("failedJob");
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find historic incident by id if engine is not available")
    void givenExistingIncidentAndNotAvailableEngine_whenFindHistoricIncidentById_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = camundaRestTestHelper.getHistoricInstancesByKey(camunda7, "testFailedJobIncident").get(0);
        HistoricIncidentDto sourceIncident = camundaRestTestHelper.findHistoricIncidentsByInstanceId(camunda7, instanceId).get(0);

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> incidentService.findHistoricIncidentById(sourceIncident.getId()))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get count of all open runtime incidents with null filter")
    void givenOpenIncidentsAndNullFilter_whenGetRuntimeIncidentCount_thenAllRuntimeIncidentsCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 3)
                .waitJobsExecution();

        //when
        long runtimeIncidentCount = incidentService.getRuntimeIncidentCount(null);

        //then
        assertThat(runtimeIncidentCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Get count of open runtime incidents only")
    void givenOpenAndResolvedIncidentsAndNullFilter_whenGetRuntimeIncidentCount_thenAllOpenRuntimeIncidentsCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 2)
                .deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs("testResolvedFailedJobIncident")
                .waitJobsExecution();

        //when
        long runtimeIncidentCount = incidentService.getRuntimeIncidentCount(null);

        //then
        assertThat(runtimeIncidentCount).isEqualTo(2);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get runtime incident count if engine is not available")
    void givenOpenRuntimeIncidentsAndNotAvailableEngine_whenGetRuntimeIncidentCount_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 2)
                .deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs("testResolvedFailedJobIncident")
                .waitJobsExecution();

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> incidentService.getRuntimeIncidentCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get count of all open historic incidents with null filter")
    void givenOpenIncidentsAndNullFilter_whenGetHistoricIncidentCount_thenAllOpenHistoricIncidentsCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 3)
                .waitJobsExecution();

        //when
        long historicIncidentCount = incidentService.getHistoricIncidentCount(null);

        //then
        assertThat(historicIncidentCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Get count of open and resolved historic incidents")
    void givenOpenAndResolvedIncidentsAndNullFilter_whenGetHistoricIncidentCount_thenAllHistoricIncidentsCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 2)
                .deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs("testResolvedFailedJobIncident")
                .waitJobsExecution();

        //when
        long historicIncidentCount = incidentService.getHistoricIncidentCount(null);

        //then
        assertThat(historicIncidentCount).isEqualTo(3);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get historic incident count if engine is not available")
    void givenResolvedIncidentAndNotAvailableEngine_whenGetHistoricIncidentCount_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 2)
                .deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs("testResolvedFailedJobIncident")
                .waitJobsExecution();

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> incidentService.getHistoricIncidentCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

}
