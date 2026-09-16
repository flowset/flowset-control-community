/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable;

import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.camunda7.dto.response.ExecutionDto;
import io.flowset.control.test_support.camunda7.dto.response.ProcessVariablesMapDto;
import io.jmix.core.DataManager;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.camunda7.dto.response.HistoricDetailDto;
import io.flowset.control.test_support.camunda7.dto.response.VariableInstanceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7VariableServiceTest extends AbstractCamunda7IntegrationTest {
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

    @ParameterizedTest
    @MethodSource("provideNonNullPrimitiveExistingVariables")
    @DisplayName("Update existing variable value for existing process")
    void givenExistingExecutionIdAndExistingVariable_whenUpdateVariableLocal_thenVariableUpdated(String variableType, Object prevValue, Object newValue) {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("myVariable", new VariableValueDto(variableType, prevValue))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testUpdateVariable").get(0);

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myVariable");
        variableInstanceData.setType(variableType);
        variableInstanceData.setValue(newValue);
        variableInstanceData.setExecutionId(processInstanceId);

        //when
        variableService.updateVariableLocal(variableInstanceData);

        //then
        VariableInstanceDto updatedRuntimeVariable = camundaRestTestHelper.getVariable(camunda7, "myVariable");
        assertThat(updatedRuntimeVariable).isNotNull();
        assertThat(updatedRuntimeVariable.getExecutionId()).isEqualTo(processInstanceId);
        assertThat(updatedRuntimeVariable.getValue()).isEqualTo(newValue);

        List<HistoricDetailDto> historyVariables = camundaRestTestHelper.getVariableLog(camunda7, processInstanceId);
        assertThat(historyVariables)
                .isNotNull()
                .hasSize(2)
                .extracting(HistoricDetailDto::getExecutionId, HistoricDetailDto::getVariableName, HistoricDetailDto::getValue)
                .contains(tuple(processInstanceId, "myVariable", prevValue),
                        tuple(processInstanceId, "myVariable", newValue)
                );
    }

    @ParameterizedTest
    @MethodSource("provideNonNullPrimitiveNewVariables")
    @DisplayName("Set new variable for existing process")
    void givenExistingExecutionIdAndNewVariable_whenUpdateVariableLocal_thenVariableUpdated(String variableType, Object newValue) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testUpdateVariable").get(0);

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myNewVariable");
        variableInstanceData.setType(variableType);
        variableInstanceData.setValue(newValue);
        variableInstanceData.setExecutionId(processInstanceId);

        //when
        variableService.updateVariableLocal(variableInstanceData);

        //then
        VariableInstanceDto updatedRuntimeVariable = camundaRestTestHelper.getVariable(camunda7, "myNewVariable");
        assertThat(updatedRuntimeVariable).isNotNull();
        assertThat(updatedRuntimeVariable.getExecutionId()).isEqualTo(processInstanceId);
        assertThat(updatedRuntimeVariable.getValue()).isEqualTo(newValue);

        List<HistoricDetailDto> historyVariables = camundaRestTestHelper.getVariableLog(camunda7, processInstanceId);
        assertThat(historyVariables)
                .isNotNull()
                .hasSize(1)
                .extracting(HistoricDetailDto::getExecutionId, HistoricDetailDto::getVariableName, HistoricDetailDto::getValue)
                .contains(tuple(processInstanceId, "myNewVariable", newValue));
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when update local variable if engine is not available")
    void givenNewVariableAndNotAvailableEngine_whenUpdateVariableLocal_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testUpdateVariable").get(0);

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myNewVariable");
        variableInstanceData.setType("String");
        variableInstanceData.setValue("newValue");
        variableInstanceData.setExecutionId(processInstanceId);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> variableService.updateVariableLocal(variableInstanceData))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Remove existing process variable")
    void givenExistingVariable_whenRemoveVariableLocal_thenVariableRemoved() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("myVariable", new VariableValueDto("String", "oldValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testUpdateVariable").get(0);

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myVariable");
        variableInstanceData.setType("String");
        variableInstanceData.setValue("oldValue");
        variableInstanceData.setExecutionId(processInstanceId);

        //when
        variableService.removeVariablesLocal(Set.of(variableInstanceData));

        //then
        ProcessVariablesMapDto myVariablesMap = camundaRestTestHelper.getVariablesByProcess(camunda7, processInstanceId);
        assertThat(myVariablesMap).isEmpty();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when remove variable if engine is not available")
    void givenExistingVariableAndNotAvailableEngine_whenRemoveVariableLocal_thenExceptionThrown() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("myVariable", new VariableValueDto("String", "oldValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testUpdateVariable").get(0);

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myVariable");
        variableInstanceData.setType("String");
        variableInstanceData.setValue("oldValue");
        variableInstanceData.setExecutionId(processInstanceId);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> variableService.removeVariablesLocal(Set.of(variableInstanceData)))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Local flag is set for variable of sub-process execution")
    void givenGlobalAndLocalVariables_whenLoadingVariables_thenLocalFlagSetForLocalVariable() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "localVariable",
                new VariableValueDto("String", "localValue"));

        VariableFilter filter = dataManager.create(VariableFilter.class);
        filter.setProcessInstanceId(processInstanceId);

        //when
        List<VariableInstanceData> variables = variableService.findRuntimeVariables(
                new VariableLoadContext().setFilter(filter));

        //then
        assertThat(variables)
                .extracting(VariableInstanceData::getName, VariableInstanceData::getLocal)
                .containsExactlyInAnyOrder(
                        tuple("globalVariable", false),
                        tuple("localVariable", true));
    }

    @Test
    @DisplayName("Create local variable in sub-process execution")
    void givenLocalFlagSet_whenUpdateVariableLocal_thenVariableCreatedInExecutionScope() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myLocalVariable");
        variableInstanceData.setType("String");
        variableInstanceData.setValue("localValue");
        variableInstanceData.setExecutionId(subProcessExecutionId);
        variableInstanceData.setLocal(true);

        //when
        variableService.updateVariableLocal(variableInstanceData);

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myLocalVariable"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId, VariableInstanceDto::getValue)
                .containsExactly(subProcessExecutionId, "localValue");
    }

    @Test
    @DisplayName("Local variable does not change global variable with the same name")
    void givenGlobalVariable_whenUpdateVariableLocalWithSameName_thenGlobalVariableUnchanged() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("sharedName", new VariableValueDto("String", "globalValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("sharedName");
        variableInstanceData.setType("String");
        variableInstanceData.setValue("localValue");
        variableInstanceData.setExecutionId(subProcessExecutionId);
        variableInstanceData.setLocal(true);

        //when
        variableService.updateVariableLocal(variableInstanceData);

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "sharedName"))
                .hasSize(2)
                .extracting(VariableInstanceDto::getExecutionId, VariableInstanceDto::getValue)
                .containsExactlyInAnyOrder(
                        tuple(processInstanceId, "globalValue"),
                        tuple(subProcessExecutionId, "localValue"));
    }

    @Test
    @DisplayName("Create non-local variable in process instance")
    void givenLocalFlagNotSet_whenUpdateVariableLocal_thenVariableCreatedInProcessInstanceScope() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myGlobalVariable");
        variableInstanceData.setType("String");
        variableInstanceData.setValue("globalValue");
        variableInstanceData.setExecutionId(processInstanceId);

        //when
        variableService.updateVariableLocal(variableInstanceData);

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myGlobalVariable"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId, VariableInstanceDto::getValue)
                .containsExactly(processInstanceId, "globalValue");
    }

    @Test
    @DisplayName("Create local binary variable in sub-process execution")
    void givenLocalFlagSetAndBinaryVariable_whenUpdateVariableBinary_thenVariableCreatedInExecutionScope() throws IOException {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        File dataFile = File.createTempFile("localBinaryVariable", ".txt");
        dataFile.deleteOnExit();
        Files.writeString(dataFile.toPath(), "binary content");

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myLocalBinaryVariable");
        variableInstanceData.setType("Bytes");
        variableInstanceData.setExecutionId(subProcessExecutionId);
        variableInstanceData.setLocal(true);

        //when
        variableService.updateVariableBinary(variableInstanceData, dataFile);

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myLocalBinaryVariable"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId)
                .isEqualTo(subProcessExecutionId);
    }

    @Test
    @DisplayName("Create local file variable in sub-process execution")
    void givenLocalFlagSetAndFileVariable_whenUpdateVariableBinary_thenVariableCreatedInExecutionScope() throws IOException {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        File dataFile = File.createTempFile("localFileVariable", ".txt");
        dataFile.deleteOnExit();
        Files.writeString(dataFile.toPath(), "file content");

        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName("myLocalFileVariable");
        variableInstanceData.setType("File");
        variableInstanceData.setExecutionId(subProcessExecutionId);
        variableInstanceData.setLocal(true);

        //when
        variableService.updateVariableBinary(variableInstanceData, dataFile);

        //then
        assertThat(camundaRestTestHelper.getVariables(camunda7, "myLocalFileVariable"))
                .singleElement()
                .extracting(VariableInstanceDto::getExecutionId, VariableInstanceDto::getType,
                        variable -> variable.getValueInfo().get("filename"))
                .containsExactly(subProcessExecutionId, "File", dataFile.getName());
    }

    protected String findSubProcessExecutionId(String processInstanceId) {
        return camundaRestTestHelper.findExecutions(camunda7, processInstanceId)
                .stream()
                .map(ExecutionDto::getId)
                .filter(executionId -> !executionId.equals(processInstanceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No child execution found for " + processInstanceId));
    }

    static Stream<Arguments> provideNonNullPrimitiveExistingVariables() {
        return Stream.of(
                Arguments.of("String", "Prev value", "New value"),
                Arguments.of("Integer", Integer.MIN_VALUE, Integer.MAX_VALUE),
                Arguments.of("Boolean", true, false),
                Arguments.of("Double", 1.5, 2.5),
                Arguments.of("Long", Long.MIN_VALUE, Long.MAX_VALUE)
        );
    }

    static Stream<Arguments> provideNonNullPrimitiveNewVariables() {
        return Stream.of(
                Arguments.of("String", "New value"),
                Arguments.of("Integer", Integer.MAX_VALUE),
                Arguments.of("Boolean", true),
                Arguments.of("Double", 2.5),
                Arguments.of("Long", Long.MAX_VALUE)
        );
    }
}
