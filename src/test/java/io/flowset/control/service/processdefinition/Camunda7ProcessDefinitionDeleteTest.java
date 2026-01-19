/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition;

import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.HistoricProcessInstanceDto;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ProcessDefinitionDeleteTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Delete active process version but not related active instances by id")
    void givenActiveProcessVersionWithActiveInstances_whenDeleteByIdWithoutInstances_thenExceptionThrownAndProcessNotDeleted() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        //when and then
        assertThatThrownBy(() -> {
            processDefinitionService.deleteById(processVersionId, false);
        })
                .isInstanceOf(RemoteProcessEngineException.class)
                .hasMessageContaining("Process definition with id: %s can't be deleted", processVersionId);


        Boolean existsProcess = camundaRestTestHelper.existsProcessById(camunda7, processVersionId);
        assertThat(existsProcess).isTrue();

    }

    @Test
    @DisplayName("Delete all active process version but not related active instances by id")
    void givenActiveProcessVersionWithActiveInstances_whenDeleteByKeyWithoutInstances_thenExceptionThrownAndProcessNotDeleted() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .startByKey("visitPlanning");

        String v1 = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String v2 = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(1);

        //when and then
        assertThatThrownBy(() -> {
            processDefinitionService.deleteAllVersionsByKey("visitPlanning", false);
        }).isInstanceOf(RemoteProcessEngineException.class);


        Boolean existsV1 = camundaRestTestHelper.existsProcessById(camunda7, v1);
        Boolean existsV2 = camundaRestTestHelper.existsProcessById(camunda7, v2);
        assertThat(existsV1).isTrue();
        assertThat(existsV2).isTrue();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when delete all versions by key if engine is not available")
    void givenExistingProcessVersionsAndNotAvailableEngine_whenDeleteAllVersionsByKey_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .startByKey("visitPlanning");

        camunda7.stop();

        //when and then
        assertThatThrownBy(() ->  processDefinitionService.deleteAllVersionsByKey("visitPlanning", true))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Delete active process version and related active instances by id")
    void givenActiveProcessVersionWithActiveInstances_whenDeleteByIdWithInstances_thenExceptionThrownAndProcessNotDeleted() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        //when and then
        processDefinitionService.deleteById(processVersionId, true);

        Boolean existsProcess = camundaRestTestHelper.existsProcessById(camunda7, processVersionId);
        assertThat(existsProcess).isFalse();

        List<ProcessInstanceDto> runtimeInstances = camundaRestTestHelper.findRuntimeProcessInstancesById(camunda7, processVersionId);
        assertThat(runtimeInstances).isEmpty();

        List<HistoricProcessInstanceDto> historyInstances = camundaRestTestHelper.findHistoryProcessInstancesById(camunda7, processVersionId);
        assertThat(historyInstances).isEmpty();

    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when delete process version by id if engine is not available")
    void givenExistingProcessVersionAndNotAvailableEngine_whenDeleteById_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> processDefinitionService.deleteById(processVersionId, true))
                .isInstanceOf(EngineConnectionFailedException.class);
    }
}
