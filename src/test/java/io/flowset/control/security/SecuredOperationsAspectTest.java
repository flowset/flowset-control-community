/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.DeploymentResultDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.jmix.core.security.AccessDeniedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@WithRunningEngine
@WithTestUser(username = "test-user-secured-operations-minimal", roles = UiMinimalRole.class)
@AuthenticatedAsUser(username = "test-user-secured-operations-minimal")
public class SecuredOperationsAspectTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Secured load returns empty list when user has no entity read access")
    void givenUserWithUiMinimalRole_whenFindLatestVersions_thenEmptyListReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        //when
        List<ProcessDefinitionData> latestVersions = processDefinitionService.findLatestVersions();

        //then
        assertThat(latestVersions).isEmpty();
    }

    @Test
    @DisplayName("Secured load returns null entity when user has no entity read access")
    void givenUserWithUiMinimalRole_whenGetProcessDefinitionById_thenNullReturned() {
        //given
        DeploymentResultDto deploymentResult = camundaRestTestHelper.createDeployment(camunda7,
                "test_support/testVisitPlanningV1.bpmn");
        String processDefinitionId = deploymentResult.getDeployedProcessDefinitions().keySet().iterator().next();

        //when
        ProcessDefinitionData processDefinition = processDefinitionService.getById(processDefinitionId);

        //then
        assertThat(processDefinition).isNull();
    }

    @Test
    @DisplayName("Secured load returns zero count when user has no entity read access")
    void givenUserWithUiMinimalRole_whenGetProcessDefinitionCount_thenZeroReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        //when
        long processDefinitionCount = processDefinitionService.getCount(null);

        //then
        assertThat(processDefinitionCount).isZero();
    }

    @Test
    @DisplayName("Secured specific operation throws access denied when user has no specific permission")
    void givenUserWithUiMinimalRole_whenStartProcess_thenAccessDeniedThrown() {
        //given
        DeploymentResultDto deploymentResult = camundaRestTestHelper.createDeployment(camunda7,
                "test_support/testVisitPlanningV1.bpmn");
        String processDefinitionId = deploymentResult.getDeployedProcessDefinitions().keySet().iterator().next();

        //when and then
        assertThatThrownBy(() -> processInstanceService.startProcessByDefinitionId(processDefinitionId,
                List.of(), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Secured entity operation throws access denied when user has no delete permission")
    void givenUserWithUiMinimalRole_whenDeleteProcessDefinition_thenAccessDeniedThrown() {
        //given
        DeploymentResultDto deploymentResult = camundaRestTestHelper.createDeployment(camunda7,
                "test_support/testVisitPlanningV1.bpmn");
        String processDefinitionId = deploymentResult.getDeployedProcessDefinitions().keySet().iterator().next();

        //when and then
        assertThatThrownBy(() -> processDefinitionService.deleteById(processDefinitionId, false))
                .isInstanceOf(AccessDeniedException.class);
    }
}
