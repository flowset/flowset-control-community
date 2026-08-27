/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable;

import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.camunda7.dto.response.ProcessVariablesMapDto;
import io.flowset.control.test_support.camunda7.dto.response.VariableInstanceDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.flowset.control.test_support.security.role.variable.TestVariableRemovePermissionRole;
import io.flowset.control.test_support.security.role.variable.TestVariableUpdatePermissionRole;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRunningEngine
public class Camunda7VariableServiceSecurityTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    VariableService variableService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("Update variable when user has update permission")
    @WithTestUser(username = "test-user-secured-variable-update", roles = TestVariableUpdatePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-variable-update")
    void givenUserWithVariableUpdatePermission_whenUpdateVariableLocal_thenVariableUpdated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable");
        String processInstanceId = sampleDataManager.getStartedInstances("testUpdateVariable").get(0);

        VariableInstanceData variableInstance = dataManager.create(VariableInstanceData.class);
        variableInstance.setName("myNewVariable");
        variableInstance.setType("String");
        variableInstance.setValue("newValue");
        variableInstance.setExecutionId(processInstanceId);

        //when
        variableService.updateVariableLocal(variableInstance);

        //then
        VariableInstanceDto updatedVariable = camundaRestTestHelper.getVariable(camunda7, "myNewVariable");
        assertThat(updatedVariable).isNotNull();
        assertThat(updatedVariable.getExecutionId()).isEqualTo(processInstanceId);
        assertThat(updatedVariable.getValue()).isEqualTo("newValue");
    }

    @Test
    @DisplayName("Remove variables when user has remove permission")
    @WithTestUser(username = "test-user-secured-variable-remove", roles = TestVariableRemovePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-variable-remove")
    void givenUserWithVariableRemovePermission_whenRemoveVariablesLocal_thenVariablesRemoved() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("myVariable", new VariableValueDto("String", "oldValue"))
                .build();
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", startProcessDto);
        String processInstanceId = sampleDataManager.getStartedInstances("testUpdateVariable").get(0);

        VariableInstanceData variableInstance = dataManager.create(VariableInstanceData.class);
        variableInstance.setName("myVariable");
        variableInstance.setType("String");
        variableInstance.setValue("oldValue");
        variableInstance.setExecutionId(processInstanceId);

        //when
        variableService.removeVariablesLocal(processInstanceId, Set.of(variableInstance));

        //then
        ProcessVariablesMapDto variablesMap = camundaRestTestHelper.getVariablesByProcess(camunda7, processInstanceId);
        assertThat(variablesMap).isEmpty();
    }
}
