/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.ProcessInstanceState;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ProcessInstanceServiceTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;


    @Test
    @DisplayName("Zero is returned as process instances count if engine is not selected")
    void givenRunningInstanceAndNoSelectedEngine_whenGetHistoricInstancesCount_thenZeroReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        resetSelectedEngine();

        //when
        long instancesCount = processInstanceService.getHistoricInstancesCount(null);

        //then
        assertThat(instancesCount).isZero();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get process instances count if engine is not available")
    void givenRunningInstanceAndNotAvailableEngine_whenGetHistoricInstancesCount_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> processInstanceService.getHistoricInstancesCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Count of all process instances is returned if filter is null")
    void givenRunningInstanceAndNullFilter_whenGetHistoricInstancesCount_thenAllInstancesCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 3);

        //when
        long instancesCount = processInstanceService.getHistoricInstancesCount(null);

        //then
        assertThat(instancesCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Null returned for existing instance if no selected engine")
    void givenRunningInstanceAndNoSelectedEngine_whenGetProcessInstanceById_thenNullReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String instanceId = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "visitPlanning").get(0);

        resetSelectedEngine();

        //when
        ProcessInstanceData foundInstance = processInstanceService.getProcessInstanceById(instanceId);

        //then
        assertThat(foundInstance).isNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get existing instance if selected engine is not available")
    void givenRunningInstanceAndNotAvailableEngine_whenGetProcessInstanceById_thenNullReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String instanceId = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "visitPlanning").get(0);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> processInstanceService.getProcessInstanceById(instanceId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get active existing instance returned by id")
    void givenRunningInstance_whenGetProcessInstanceById_thenFoundInstanceReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");
        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String instanceId = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processId).get(0).getId();

        //when
        ProcessInstanceData foundInstance = processInstanceService.getProcessInstanceById(instanceId);

        //then
        assertThat(foundInstance).isNotNull();
        assertThat(foundInstance.getId()).isEqualTo(instanceId);

        //check state flags
        assertThat(foundInstance.getState()).isEqualTo(ProcessInstanceState.ACTIVE);
        assertThat(foundInstance.getSuspended()).isFalse();
        assertThat(foundInstance.getComplete()).isFalse();
        assertThat(foundInstance.getInternallyTerminated()).isFalse();
        assertThat(foundInstance.getExternallyTerminated()).isFalse();
        assertThat(foundInstance.getFinished()).isFalse();

        //check process definition
        assertThat(foundInstance.getProcessDefinitionId()).isEqualTo(processId);
        assertThat(foundInstance.getProcessDefinitionKey()).isEqualTo("visitPlanning");
        assertThat(foundInstance.getProcessDefinitionVersion()).isEqualTo(1);

        assertThat(foundInstance.getStartTime()).isNotNull();
        assertThat(foundInstance.getEndTime()).isNull();
        assertThat(foundInstance.getBusinessKey()).isNull();
    }

    @Test
    @DisplayName("Get suspended existing instance returned by id")
    void givenSuspendedInstance_whenGetProcessInstanceById_thenFoundInstanceReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String instanceId = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processId).get(0).getId();

        camundaRestTestHelper.suspendInstanceById(camunda7, instanceId);

        //when
        ProcessInstanceData foundInstance = processInstanceService.getProcessInstanceById(instanceId);

        //then
        assertThat(foundInstance).isNotNull();
        assertThat(foundInstance.getId()).isEqualTo(instanceId);

        //check state flags
        assertThat(foundInstance.getState()).isEqualTo(ProcessInstanceState.SUSPENDED);
        assertThat(foundInstance.getSuspended()).isTrue();
        assertThat(foundInstance.getComplete()).isFalse();
        assertThat(foundInstance.getInternallyTerminated()).isFalse();
        assertThat(foundInstance.getExternallyTerminated()).isFalse();
        assertThat(foundInstance.getFinished()).isFalse();

        //check process definition
        assertThat(foundInstance.getProcessDefinitionId()).isEqualTo(processId);
        assertThat(foundInstance.getProcessDefinitionKey()).isEqualTo("visitPlanning");
        assertThat(foundInstance.getProcessDefinitionVersion()).isEqualTo(1);

        assertThat(foundInstance.getStartTime()).isNotNull();
        assertThat(foundInstance.getEndTime()).isNull();
        assertThat(foundInstance.getBusinessKey()).isNull();
    }

    @Test
    @DisplayName("Get externally terminated existing instance returned by id")
    void givenExternallyTerminatedInstance_whenGetProcessInstanceById_thenFoundInstanceReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String instanceId = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processId).get(0).getId();

        camundaRestTestHelper.terminateExternallyInstance(camunda7, instanceId);

        //when
        ProcessInstanceData foundInstance = processInstanceService.getProcessInstanceById(instanceId);

        //then
        assertThat(foundInstance).isNotNull();
        assertThat(foundInstance.getId()).isEqualTo(instanceId);

        //check state flags
        assertThat(foundInstance.getState()).isEqualTo(ProcessInstanceState.COMPLETED);
        assertThat(foundInstance.getSuspended()).isFalse();
        assertThat(foundInstance.getComplete()).isFalse();
        assertThat(foundInstance.getInternallyTerminated()).isFalse();
        assertThat(foundInstance.getExternallyTerminated()).isTrue();
        assertThat(foundInstance.getFinished()).isTrue();

        //check process definition
        assertThat(foundInstance.getProcessDefinitionId()).isEqualTo(processId);
        assertThat(foundInstance.getProcessDefinitionKey()).isEqualTo("visitPlanning");
        assertThat(foundInstance.getProcessDefinitionVersion()).isEqualTo(1);

        assertThat(foundInstance.getStartTime()).isNotNull();
        assertThat(foundInstance.getEndTime()).isNotNull();
        assertThat(foundInstance.getDeleteReason()).isNull();
        assertThat(foundInstance.getBusinessKey()).isNull();
    }

    @Test
    @DisplayName("Get completed existing instance returned by id")
    void givenCompletedInstance_whenGetProcessInstanceById_thenFoundInstanceReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testCompletedInstance.bpmn")
                .startByKey("testCompletedInstance");

        String processId = sampleDataManager.getDeployedProcessVersions("testCompletedInstance").get(0);
        String instanceId = camundaRestTestHelper.getHistoryInstancesById(camunda7, processId).get(0).getId();

        //when
        ProcessInstanceData foundInstance = processInstanceService.getProcessInstanceById(instanceId);

        //then
        assertThat(foundInstance).isNotNull();
        assertThat(foundInstance.getId()).isEqualTo(instanceId);

        //check state flags
        assertThat(foundInstance.getState()).isEqualTo(ProcessInstanceState.COMPLETED);
        assertThat(foundInstance.getSuspended()).isFalse();
        assertThat(foundInstance.getComplete()).isTrue();
        assertThat(foundInstance.getInternallyTerminated()).isFalse();
        assertThat(foundInstance.getExternallyTerminated()).isFalse();
        assertThat(foundInstance.getFinished()).isTrue();

        //check process definition
        assertThat(foundInstance.getProcessDefinitionId()).isEqualTo(processId);
        assertThat(foundInstance.getProcessDefinitionKey()).isEqualTo("testCompletedInstance");
        assertThat(foundInstance.getProcessDefinitionVersion()).isEqualTo(1);

        assertThat(foundInstance.getStartTime()).isNotNull();
        assertThat(foundInstance.getEndTime()).isNotNull();
        assertThat(foundInstance.getDeleteReason()).isNull();
        assertThat(foundInstance.getBusinessKey()).isNull();
    }

    @Test
    @DisplayName("Null instance returned by non-existing id")
    void givenNonExistingInstanceId_whenGetProcessInstanceById_thenNullReturned() {
        //given
        String nonExistingId = UUID.randomUUID().toString();

        //when
        ProcessInstanceData foundInstance = processInstanceService.getProcessInstanceById(nonExistingId);

        //then
        assertThat(foundInstance).isNull();
    }

    @Test
    @DisplayName("Get count of not completed instances by existing process definition id")
    void givenExistingProcessVersions_whenGetCountByProcessDefinitionId_thenNotCompletedInstancesCountReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .startByKey("visitPlanning", 2);

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);

        //when
        long instancesCount = processInstanceService.getCountByProcessDefinitionId(processId);

        //then
        assertThat(instancesCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Get count of not completed instances by existing process definition key")
    void givenExistingProcessVersions_whenGetCountByProcessDefinitionKey_thenNotCompletedInstancesCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .startByKey("visitPlanning", 2);

        String processKey = "visitPlanning";

        //when
        long instancesCount = processInstanceService.getCountByProcessDefinitionKey(processKey);

        //then
        assertThat(instancesCount).isEqualTo(4);
    }
}
