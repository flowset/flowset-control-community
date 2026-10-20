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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7VariableServiceUpdateTest extends AbstractCamunda7IntegrationTest {
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
    @DisplayName("Update value of existing local variable")
    void givenExistingLocalVariable_whenUpdateVariableLocal_thenValueUpdatedInSameExecution() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "myLocalVariable",
                new VariableValueDto("String", "oldValue"));

        VariableInstanceData variableInstanceData = createVariableData("myLocalVariable", "newValue",
                subProcessExecutionId, true);

        //when
        variableService.updateVariableLocal(variableInstanceData);

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myLocalVariable"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId, VariableInstanceDto::getValue)
                .containsExactly(subProcessExecutionId, "newValue");
    }

    @Test
    @DisplayName("Local variable of the process instance execution stays in the process instance scope")
    void givenLocalFlagSetAndProcessInstanceExecutionId_whenUpdateVariableLocal_thenVariableCreatedInProcessInstanceScope() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        VariableInstanceData variableInstanceData = createVariableData("myVariable", "someValue",
                processInstanceId, true);

        //when
        variableService.updateVariableLocal(variableInstanceData);

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myVariable"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId, VariableInstanceDto::getValue)
                .containsExactly(processInstanceId, "someValue");
    }

    @Test
    @DisplayName("NullPointerException thrown when updated variable has no execution id")
    void givenVariableWithoutExecutionId_whenUpdateVariableLocal_thenExceptionThrown() {
        //given
        VariableInstanceData variableInstanceData = createVariableData("myVariable", "someValue", null, null);

        //when and then
        assertThatThrownBy(() -> variableService.updateVariableLocal(variableInstanceData))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when update local variable of execution if engine is not available")
    void givenLocalFlagSetAndNotAvailableEngine_whenUpdateVariableLocal_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        VariableInstanceData variableInstanceData = createVariableData("myLocalVariable", "localValue",
                subProcessExecutionId, true);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> variableService.updateVariableLocal(variableInstanceData))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Create non-local binary variable in process instance")
    void givenLocalFlagNotSetAndBinaryVariable_whenUpdateVariableBinary_thenVariableCreatedInProcessInstanceScope() throws IOException {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();

        File dataFile = createDataFile("globalBinaryVariable", "binary content");

        VariableInstanceData variableInstanceData = createVariableData("myBinaryVariable", null,
                processInstanceId, null);
        variableInstanceData.setType("Bytes");

        //when
        variableService.updateVariableBinary(variableInstanceData, dataFile);

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myBinaryVariable"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId)
                .isEqualTo(processInstanceId);
    }

    @Test
    @DisplayName("Update value of existing local binary variable")
    void givenExistingLocalBinaryVariable_whenUpdateVariableBinary_thenValueReplacedInSameExecution() throws IOException {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        VariableInstanceData variableInstanceData = createVariableData("myLocalBinaryVariable", null,
                subProcessExecutionId, true);
        variableInstanceData.setType("Bytes");

        variableService.updateVariableBinary(variableInstanceData, createDataFile("firstContent", "first content"));

        //when
        variableService.updateVariableBinary(variableInstanceData, createDataFile("secondContent", "second content"));

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myLocalBinaryVariable"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId)
                .isEqualTo(subProcessExecutionId);
    }

    VariableInstanceData createVariableData(String name, @Nullable Object value, @Nullable String executionId,
                                            @Nullable Boolean local) {
        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName(name);
        variableInstanceData.setType("String");
        variableInstanceData.setValue(value);
        variableInstanceData.setExecutionId(executionId);
        variableInstanceData.setLocal(local);

        return variableInstanceData;
    }

    File createDataFile(String prefix, String content) throws IOException {
        File dataFile = File.createTempFile(prefix, ".txt");
        dataFile.deleteOnExit();
        Files.writeString(dataFile.toPath(), content);

        return dataFile;
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
