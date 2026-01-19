/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition;

import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.ProcessDefinitionDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeProcessInstanceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ProcessDefinitionSuspendTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Suspend active process version not having instance by id")
    void givenActiveProcessVersionWithoutInstances_whenSuspendByIdWithoutInstances_thenProcessWithInstancesSuspended() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        //when
        processDefinitionService.suspendById(processVersionId, true);

        //then
        ProcessDefinitionDto foundProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);
        assertThat(foundProcess).isNotNull();
        assertThat(foundProcess.isSuspended()).isTrue();
    }

    @Test
    @DisplayName("Suspend active process version and related active instances by id")
    void givenActiveProcessVersionWithInstances_whenSuspendByIdWithInstances_thenProcessWithInstancesSuspended() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        RuntimeProcessInstanceDto activeInstance = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processVersionId).get(0);

        //when
        processDefinitionService.suspendById(processVersionId, true);

        //then
        ProcessDefinitionDto foundProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);
        assertThat(foundProcess).isNotNull();
        assertThat(foundProcess.isSuspended()).isTrue();

        List<RuntimeProcessInstanceDto> foundInstances = camundaRestTestHelper.getRuntimeInstancesById(camunda7, foundProcess.getId());

        assertThat(foundInstances)
                .hasSize(1)
                .first()
                .satisfies(foundInstance -> {
                    assertThat(foundInstance.getId()).isEqualTo(activeInstance.getId());
                    assertThat(foundInstance.getSuspended()).isTrue();
                });
    }

    @Test
    @DisplayName("Suspend active process version but not related instances by id")
    void givenActiveProcessVersionWithInstances_whenSuspendByIdWithoutInstances_thenProcessSuspendedAndInstancesNotSuspended() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        RuntimeProcessInstanceDto activeInstance = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processVersionId).get(0);

        //when
        processDefinitionService.suspendById(processVersionId, false);

        //then
        ProcessDefinitionDto foundProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);
        assertThat(foundProcess).isNotNull();
        assertThat(foundProcess.isSuspended()).isTrue();

        List<RuntimeProcessInstanceDto> foundInstances = camundaRestTestHelper.getRuntimeInstancesById(camunda7, foundProcess.getId());

        assertThat(foundInstances)
                .hasSize(1)
                .first()
                .satisfies(foundInstance -> {
                    assertThat(foundInstance.getId()).isEqualTo(activeInstance.getId());
                    assertThat(foundInstance.getSuspended()).isFalse();
                });
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when suspend process version by id if engine is not available")
    void givenActiveProcessVersionAndNotAvailableEngine_whenSuspendById_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);
        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> processDefinitionService.suspendById(processVersionId, false))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Suspend all active process versions but not related instances by key")
    void givenAllActiveProcessVersionsWithInstances_whenSuspendAllVersionsByKeyWithoutInstances_thenProcessSuspendedAndInstancesNotSuspended() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .startByKey("visitPlanning");

        List<String> activeProcesses = camundaRestTestHelper.getActiveProcessesIdsByKey(camunda7, "visitPlanning");
        List<String> activeInstances = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "visitPlanning");

        //when
        processDefinitionService.suspendAllVersionsByKey("visitPlanning", false);

        //then
        List<String> foundSuspendedProcesses = camundaRestTestHelper.getSuspendedProcessesIdsByKey(camunda7, "visitPlanning");
        assertThat(foundSuspendedProcesses)
                .isNotNull()
                .hasSize(2)
                .containsAll(activeProcesses);

        //check each process instance
        List<String> foundActiveInstances = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "visitPlanning");
        assertThat(foundActiveInstances)
                .hasSize(2)
                .containsAll(activeInstances);

    }

    @Test
    @DisplayName("Suspend all active process versions and related instances by key")
    void givenAllActiveProcessVersionsWithInstances_whenSuspendAllVersionsByKeyWithInstances_thenProcessAndInstancesSuspended() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .startByKey("visitPlanning");

        List<String> activeProcesses = camundaRestTestHelper.getActiveProcessesIdsByKey(camunda7, "visitPlanning");
        List<String> activeInstances = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "visitPlanning");

        //when
        processDefinitionService.suspendAllVersionsByKey("visitPlanning", true);

        //then
        List<String> foundSuspendedProcesses = camundaRestTestHelper.getSuspendedProcessesIdsByKey(camunda7, "visitPlanning");
        assertThat(foundSuspendedProcesses)
                .isNotNull()
                .hasSize(2)
                .containsAll(activeProcesses);

        List<String> foundSuspendedInstances = camundaRestTestHelper.getSuspendedRuntimeInstancesByKey(camunda7, "visitPlanning");
        assertThat(foundSuspendedInstances)
                .hasSize(2)
                .containsAll(activeInstances);

    }

    @Test
    @DisplayName("No error if suspend all suspended process versions and related instances by key")
    void givenAllSuspendedProcessVersionsWithInstances_whenSuspendAllVersionsByKeyWithInstances_thenProcessAndInstancesNotChanged() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .suspendByKey("visitPlanning", true)
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .startByKey("visitPlanning")
                .suspendByKey("visitPlanning", true);

        List<String> suspendedProcesses = camundaRestTestHelper.getSuspendedProcessesIdsByKey(camunda7, "visitPlanning");
        List<String> suspendedInstances = camundaRestTestHelper.getSuspendedRuntimeInstancesByKey(camunda7, "visitPlanning");

        //when
        processDefinitionService.suspendAllVersionsByKey("visitPlanning", true);

        //then
        List<String> foundSuspendedProcesses = camundaRestTestHelper.getSuspendedProcessesIdsByKey(camunda7, "visitPlanning");
        assertThat(foundSuspendedProcesses)
                .isNotNull()
                .hasSize(2)
                .containsAll(suspendedProcesses);

        List<String> foundSuspendedInstances = camundaRestTestHelper.getSuspendedRuntimeInstancesByKey(camunda7, "visitPlanning");
        assertThat(foundSuspendedInstances)
                .hasSize(2)
                .containsAll(suspendedInstances);

    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when suspend process versions by key if engine is not available")
    void givenActiveProcessVersionsAndNotAvailableEngine_whenSuspendAllVersionsByKey_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> processDefinitionService.suspendAllVersionsByKey("visitPlanning", true))
                .isInstanceOf(EngineConnectionFailedException.class);
    }
}
