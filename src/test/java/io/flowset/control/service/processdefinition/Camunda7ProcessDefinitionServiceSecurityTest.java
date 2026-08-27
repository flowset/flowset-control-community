/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition;

import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.ProcessDefinitionDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionActivatePermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDeletePermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionSuspendPermissionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRunningEngine
public class Camunda7ProcessDefinitionServiceSecurityTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Test
    @DisplayName("Activate process definition by id when user has activate permission")
    @WithTestUser(username = "test-user-secured-process-definition-activate",
            roles = TestProcessDefinitionActivatePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-definition-activate")
    void givenUserWithProcessDefinitionActivatePermission_whenActivateById_thenProcessDefinitionActivated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        String processDefinitionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);
        camundaRestTestHelper.suspendProcessById(camunda7, "vacation_approval", processDefinitionId, true);

        //when
        processDefinitionService.activateById(processDefinitionId, true);

        //then
        ProcessDefinitionDto processDefinition = camundaRestTestHelper.getProcessById(camunda7, processDefinitionId);
        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.isSuspended()).isFalse();
    }

    @Test
    @DisplayName("Suspend process definition by id when user has suspend permission")
    @WithTestUser(username = "test-user-secured-process-definition-suspend",
            roles = TestProcessDefinitionSuspendPermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-definition-suspend")
    void givenUserWithProcessDefinitionSuspendPermission_whenSuspendById_thenProcessDefinitionSuspended() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        String processDefinitionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        //when
        processDefinitionService.suspendById(processDefinitionId, false);

        //then
        ProcessDefinitionDto processDefinition = camundaRestTestHelper.getProcessById(camunda7, processDefinitionId);
        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.isSuspended()).isTrue();
    }

    @Test
    @DisplayName("Delete process definition by id when user has delete permission")
    @WithTestUser(username = "test-user-secured-process-definition-delete",
            roles = TestProcessDefinitionDeletePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-definition-delete")
    void givenUserWithProcessDefinitionDeletePermission_whenDeleteById_thenProcessDefinitionDeleted() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        String processDefinitionId = sampleDataManager.getDeployedProcessVersions("vacation_approval").get(0);

        //when
        processDefinitionService.deleteById(processDefinitionId, false);

        //then
        assertThat(camundaRestTestHelper.existsProcessById(camunda7, processDefinitionId)).isFalse();
    }
}
