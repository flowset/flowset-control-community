/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable;

import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.camunda7.dto.response.ExecutionDto;
import io.flowset.control.test_support.camunda7.dto.response.VariableInstanceDto;
import io.jmix.core.DataManager;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7VariableServiceRemoveTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    VariableService variableService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("Remove local and global variables")
    void givenLocalAndGlobalVariables_whenRemoveVariablesLocal_thenEachRemovedFromItsOwnExecution() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "localVariable",
                new VariableValueDto("String", "localValue"));

        VariableInstanceData globalVariable = createVariableData("globalVariable", processInstanceId);
        VariableInstanceData localVariable = createVariableData("localVariable", subProcessExecutionId);

        //when
        variableService.removeVariablesLocal(Set.of(globalVariable, localVariable));

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "globalVariable")).isEmpty();
        assertThat(camundaRestTestHelper.getVariables(camunda7, "localVariable")).isEmpty();
    }

    @Test
    @DisplayName("Global variable is not removed if remove a local variable with the same name")
    void givenSameNameInTwoScopes_whenRemoveVariablesLocal_thenGlobalVariableRemains() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("sharedName", new VariableValueDto("String", "globalValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "sharedName",
                new VariableValueDto("String", "localValue"));

        VariableInstanceData localVariable = createVariableData("sharedName", subProcessExecutionId);

        //when
        variableService.removeVariablesLocal(Set.of(localVariable));

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "sharedName"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId, VariableInstanceDto::getValue)
                .containsExactly(processInstanceId, "globalValue");
    }

    @Test
    @DisplayName("Several variables of the same execution are removed at once")
    void givenTwoLocalVariablesInSameExecution_whenRemoveVariablesLocal_thenBothRemoved() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "firstLocalVariable",
                new VariableValueDto("String", "firstValue"));
        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "secondLocalVariable",
                new VariableValueDto("String", "secondValue"));

        VariableInstanceData firstVariable = createVariableData("firstLocalVariable", subProcessExecutionId);
        VariableInstanceData secondVariable = createVariableData("secondLocalVariable", subProcessExecutionId);

        //when
        variableService.removeVariablesLocal(Set.of(firstVariable, secondVariable));

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "firstLocalVariable")).isEmpty();
        assertThat(camundaRestTestHelper.getVariables(camunda7, "secondLocalVariable")).isEmpty();
    }

    @Test
    @DisplayName("NullPointerException thrown when a variable has no execution id")
    void givenVariableWithoutExecutionId_whenRemoveVariablesLocal_thenExceptionThrown() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("myVariable", new VariableValueDto("String", "oldValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testUpdateVariable").getFirst();

        VariableInstanceData existingVariable = createVariableData("myVariable", processInstanceId);
        VariableInstanceData variableWithoutExecution = createVariableData("otherVariable", null);

        //when and then
        assertThatThrownBy(() ->
                variableService.removeVariablesLocal(Set.of(existingVariable, variableWithoutExecution)))
                .isInstanceOf(NullPointerException.class);

        // The whole set is grouped before any engine call, so the valid variable is left untouched.
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myVariable")).hasSize(1);
    }

    @Test
    @DisplayName("No variables removed when the passed set is empty")
    void givenEmptySet_whenRemoveVariablesLocal_thenNoVariablesRemoved() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("myVariable", new VariableValueDto("String", "oldValue"))
                .build();

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", startProcessDto);

        //when
        variableService.removeVariablesLocal(Set.of());

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myVariable")).hasSize(1);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when remove local variable if engine is not available")
    void givenLocalVariableAndNotAvailableEngine_whenRemoveVariablesLocal_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "localVariable",
                new VariableValueDto("String", "localValue"));

        VariableInstanceData localVariable = createVariableData("localVariable", subProcessExecutionId);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> variableService.removeVariablesLocal(Set.of(localVariable)))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    VariableInstanceData createVariableData(String name, @Nullable String executionId) {
        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName(name);
        variableInstanceData.setType("String");
        variableInstanceData.setExecutionId(executionId);

        return variableInstanceData;
    }

    String findSubProcessExecutionId(String processInstanceId) {
        return camundaRestTestHelper.findExecutions(camunda7, processInstanceId)
                .stream()
                .map(ExecutionDto::getId)
                .filter(executionId -> !executionId.equals(processInstanceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No child execution found for " + processInstanceId));
    }
}
