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
public class Camunda7ProcessDefinitionActivateTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Activate suspended process version not having instance by id")
    void givenSuspendedProcessVersionWithoutInstances_whenActivateByIdWithoutInstances_thenProcessWithInstancesActivated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);
        camundaRestTestHelper.suspendProcessById(camunda7, "vacation_approval", processVersionId, true);
        ProcessDefinitionDto suspendedProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);

        //when
        processDefinitionService.activateById(processVersionId, true);

        //then
        ProcessDefinitionDto foundProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);
        assertThat(foundProcess).isNotNull();
        assertThat(foundProcess.isSuspended()).isNotEqualTo(suspendedProcess.isSuspended());
        assertThat(foundProcess.isSuspended()).isFalse();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when activate definition by id if engine is not available")
    void givenSuspendedProcessVersionAndNotAvailableEngine_whenActivateById_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);
        camundaRestTestHelper.suspendProcessById(camunda7, "vacation_approval", processVersionId, true);
        ProcessDefinitionDto suspendedProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> processDefinitionService.activateById(suspendedProcess.getId(), true))
                .isInstanceOf(EngineConnectionFailedException.class);
    }


    @Test
    @DisplayName("Activate suspended process version and related suspended instances by id")
    void givenSuspendedProcessVersionWithInstances_whenActivateByIdWithInstances_thenProcessWithInstancesActivated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);
        camundaRestTestHelper.suspendProcessById(camunda7, "vacation_approval", processVersionId, true);

        ProcessDefinitionDto suspendedProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);
        RuntimeProcessInstanceDto suspendedInstance = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processVersionId).get(0);

        //when
        processDefinitionService.activateById(processVersionId, true);

        //then
        ProcessDefinitionDto foundProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);
        assertThat(foundProcess).isNotNull();
        assertThat(foundProcess.isSuspended()).isNotEqualTo(suspendedProcess.isSuspended());
        assertThat(foundProcess.isSuspended()).isFalse();

        List<RuntimeProcessInstanceDto> foundInstances = camundaRestTestHelper.getRuntimeInstancesById(camunda7, foundProcess.getId());

        assertThat(foundInstances)
                .hasSize(1)
                .first()
                .satisfies(foundInstance -> {
                    assertThat(foundInstance.getId()).isEqualTo(suspendedInstance.getId());
                    assertThat(foundInstance.getSuspended()).isNotEqualTo(suspendedInstance.getSuspended());
                    assertThat(foundInstance.getSuspended()).isFalse();
                });
    }

    @Test
    @DisplayName("Activate suspended process version but not related instances by id")
    void givenSuspendedProcessVersionWithInstances_whenActivateByIdWithoutInstances_thenProcessActivatedAndInstancesNotActivated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        String processVersionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);
        camundaRestTestHelper.suspendProcessById(camunda7, "vacation_approval", processVersionId, true);

        ProcessDefinitionDto suspendedProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);
        RuntimeProcessInstanceDto suspendedInstance = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processVersionId).get(0);

        //when
        processDefinitionService.activateById(processVersionId, false);

        //then
        ProcessDefinitionDto foundProcess = camundaRestTestHelper.getProcessById(camunda7, processVersionId);
        assertThat(foundProcess).isNotNull();
        assertThat(foundProcess.isSuspended()).isNotEqualTo(suspendedProcess.isSuspended());
        assertThat(foundProcess.isSuspended()).isFalse();

        List<RuntimeProcessInstanceDto> foundInstances = camundaRestTestHelper.getRuntimeInstancesById(camunda7, foundProcess.getId());

        assertThat(foundInstances)
                .hasSize(1)
                .first()
                .satisfies(foundInstance -> {
                    assertThat(foundInstance.getId()).isEqualTo(suspendedInstance.getId());
                    assertThat(foundInstance.getSuspended()).isEqualTo(suspendedInstance.getSuspended());
                    assertThat(foundInstance.getSuspended()).isTrue();
                });
    }


    @Test
    @DisplayName("Activate all suspended process versions but not related instances by key")
    void givenAllSuspendedProcessVersionsWithInstances_whenActivateAllVersionsByKeyWithoutInstances_thenProcessActivatedAndInstancesNotActivated() {
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
        processDefinitionService.activateAllVersionsByKey("visitPlanning", false);

        //then
        List<String> foundActiveProcesses = camundaRestTestHelper.getActiveProcessesIdsByKey(camunda7, "visitPlanning");
        assertThat(foundActiveProcesses)
                .isNotNull()
                .hasSize(2)
                .containsAll(suspendedProcesses);

        //check each process instance
        List<String> foundSuspendedInstances = camundaRestTestHelper.getSuspendedRuntimeInstancesByKey(camunda7, "visitPlanning");
        assertThat(foundSuspendedInstances)
                .hasSize(2)
                .containsAll(suspendedInstances);

    }

    @Test
    @DisplayName("Activate all suspended process versions and related instances by key")
    void givenAllSuspendedProcessVersionsWithInstances_whenActivateAllVersionsByKeyWithInstances_thenProcessAndInstancesActivated() {
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
        processDefinitionService.activateAllVersionsByKey("visitPlanning", true);

        //then
        List<String> foundActiveProcesses = camundaRestTestHelper.getActiveProcessesIdsByKey(camunda7, "visitPlanning");
        assertThat(foundActiveProcesses)
                .isNotNull()
                .hasSize(2)
                .containsAll(suspendedProcesses);

        List<String> foundActiveInstances = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "visitPlanning");
        assertThat(foundActiveInstances)
                .hasSize(2)
                .containsAll(suspendedInstances);

    }

    @Test
    @DisplayName("No error if activate all active process versions and related instances by key")
    void givenAllActiveProcessVersionsWithInstances_whenActivateAllVersionsByKeyWithInstances_thenProcessAndInstancesNotChanged() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .startByKey("visitPlanning");

        List<String> activeProcesses = camundaRestTestHelper.getActiveProcessesIdsByKey(camunda7, "visitPlanning");
        List<String> activeInstances = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "visitPlanning");

        //when
        processDefinitionService.activateAllVersionsByKey("visitPlanning", true);

        //then
        List<String> foundActiveProcesses = camundaRestTestHelper.getActiveProcessesIdsByKey(camunda7, "visitPlanning");
        assertThat(foundActiveProcesses)
                .isNotNull()
                .hasSize(2)
                .containsAll(activeProcesses);

        List<String> foundActiveInstances = camundaRestTestHelper.getActiveRuntimeInstancesByKey(camunda7, "visitPlanning");
        assertThat(foundActiveInstances)
                .hasSize(2)
                .containsAll(activeInstances);

    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when activate procee by key if engine is not available")
    void givenActiveProcessVersionsAndNotAvailableEngine_whenActivateByKey_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .suspendByKey("visitPlanning", true)
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .suspendByKey("visitPlanning", true);


        camunda7.stop();

        //when and then
        assertThatThrownBy(() ->  processDefinitionService.activateAllVersionsByKey("visitPlanning", true))
                .isInstanceOf(EngineConnectionFailedException.class);
    }
}
