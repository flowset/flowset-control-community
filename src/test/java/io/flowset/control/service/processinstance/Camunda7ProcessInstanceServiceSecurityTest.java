/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.HistoricProcessInstanceDto;
import io.flowset.control.test_support.camunda7.dto.response.DeploymentResultDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeProcessInstanceDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionStartPermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceActivatePermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceSuspendPermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceTerminatePermissionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRunningEngine
public class Camunda7ProcessInstanceServiceSecurityTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Start process instance by definition id when user has start permission")
    @WithTestUser(username = "test-user-secured-process-instance-start",
            roles = TestProcessDefinitionStartPermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-instance-start")
    void givenUserWithProcessDefinitionStartPermission_whenStartProcessByDefinitionId_thenProcessInstanceStarted() {
        //given
        DeploymentResultDto deploymentResult = camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String processDefinitionId = deploymentResult.getDeployedProcessDefinitions().keySet().iterator().next();

        //when
        ProcessInstanceData startedInstance = processInstanceService.startProcessByDefinitionId(processDefinitionId,
                List.of(), null);

        //then
        assertThat(startedInstance).isNotNull();
        assertThat(startedInstance.getInstanceId()).isNotBlank();
        RuntimeProcessInstanceDto runtimeInstance = camundaRestTestHelper.findRuntimeInstance(camunda7, startedInstance.getInstanceId());
        assertThat(runtimeInstance).isNotNull();
        assertThat(runtimeInstance.getDefinitionId()).isEqualTo(processDefinitionId);
    }

    @Test
    @DisplayName("Activate process instance by id when user has activate permission")
    @WithTestUser(username = "test-user-secured-process-instance-activate",
            roles = TestProcessInstanceActivatePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-instance-activate")
    void givenUserWithProcessInstanceActivatePermission_whenActivateById_thenProcessInstanceActivated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");
        String processDefinitionId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String instanceId = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processDefinitionId).get(0).getId();
        camundaRestTestHelper.suspendInstanceById(camunda7, instanceId);

        //when
        processInstanceService.activateById(instanceId);

        //then
        RuntimeProcessInstanceDto runtimeInstance = camundaRestTestHelper.getRuntimeInstanceById(camunda7, instanceId);
        assertThat(runtimeInstance).isNotNull();
        assertThat(runtimeInstance.getSuspended()).isFalse();
    }

    @Test
    @DisplayName("Suspend process instance by id when user has suspend permission")
    @WithTestUser(username = "test-user-secured-process-instance-suspend",
            roles = TestProcessInstanceSuspendPermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-instance-suspend")
    void givenUserWithProcessInstanceSuspendPermission_whenSuspendById_thenProcessInstanceSuspended() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");
        String instanceId = sampleDataManager.getStartedInstances("visitPlanning").get(0);

        //when
        processInstanceService.suspendById(instanceId);

        //then
        RuntimeProcessInstanceDto runtimeInstance = camundaRestTestHelper.getRuntimeInstanceById(camunda7, instanceId);
        assertThat(runtimeInstance).isNotNull();
        assertThat(runtimeInstance.getSuspended()).isTrue();
    }

    @Test
    @DisplayName("Terminate process instance by id when user has terminate permission")
    @WithTestUser(username = "test-user-secured-process-instance-terminate",
            roles = TestProcessInstanceTerminatePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-instance-terminate")
    void givenUserWithProcessInstanceTerminatePermission_whenTerminateById_thenProcessInstanceTerminated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");
        String processDefinitionId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String instanceId = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processDefinitionId).get(0).getId();

        //when
        processInstanceService.terminateById(instanceId);

        //then
        assertThat(camundaRestTestHelper.getRuntimeInstanceById(camunda7, instanceId)).isNull();
        HistoricProcessInstanceDto historicInstance = camundaRestTestHelper.getHistoryInstanceById(camunda7, instanceId);
        assertThat(historicInstance).isNotNull();
        assertThat(historicInstance.getState()).isEqualTo("EXTERNALLY_TERMINATED");
    }
}
