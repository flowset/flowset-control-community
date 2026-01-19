/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable;

import io.flowset.control.exception.EngineConnectionFailedException;
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
        variableService.removeVariablesLocal(processInstanceId, Set.of(variableInstanceData));

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
        assertThatThrownBy(() -> variableService.removeVariablesLocal(processInstanceId, Set.of(variableInstanceData)))
                .isInstanceOf(EngineConnectionFailedException.class);
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
