/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.usertask;

import io.flowset.control.exception.EngineConnectionFailedException;
import io.jmix.core.DataManager;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.HistoricUserTaskDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeProcessInstanceDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeUserTaskDto;
import io.flowset.control.test_support.camunda7.dto.response.VariableInstanceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7UserTaskCompleteTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    UserTaskService userTaskService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DataManager dataManager;


    @Test
    @DisplayName("Complete existing active task by id without variables")
    void givenActiveTaskIdAndEmptyMapWithVariables_whenCompleteById_thenTaskCompleted() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");

        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithoutAssignee").get(0);

        String taskId = userTask.getId();

        //when
        userTaskService.completeTaskById(taskId, List.of());

        //then
        Boolean activeTaskExists = camundaRestTestHelper.runtimeUserTaskExists(camunda7, taskId);
        assertThat(activeTaskExists).isFalse();

        HistoricUserTaskDto historyUserTask = camundaRestTestHelper.findHistoryUserTask(camunda7, taskId);
        assertThat(historyUserTask).isNotNull();
        assertThat(historyUserTask.getEndTime()).isNotNull();
        assertThat(historyUserTask.getTaskDefinitionKey()).isEqualTo("taskWithoutAssignee");
    }

    @ParameterizedTest
    @MethodSource("provideNonNullPrimitiveVariables")
    @DisplayName("Complete existing active task by id with non-null primitive variable")
    void givenActiveTaskIdAndNotNullPrimitiveVariable_whenCompleteById_thenTaskCompletedAndVariableSet(String variableType, Object variableValue) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskVariable.bpmn")
                .startByKey("testUserTaskVariable");

        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "testUserTaskVariable").get(0);

        String taskId = userTask.getId();
        VariableInstanceData variableInstance = createVariableInstance("myVar", variableType, variableValue);

        //when
        userTaskService.completeTaskById(taskId, List.of(variableInstance));

        //then: check task completed and variable is set for process instance
        HistoricUserTaskDto historyUserTask = camundaRestTestHelper.findHistoryUserTask(camunda7, taskId);
        assertThat(historyUserTask).isNotNull();
        assertThat(historyUserTask.getEndTime()).isNotNull();
        assertThat(historyUserTask.getTaskDefinitionKey()).isEqualTo("firstUserTask");

        VariableInstanceDto variable = camundaRestTestHelper.getVariable(camunda7, "myVar");
        assertThat(variable).isNotNull();
        assertThat(variable.getName()).isEqualTo("myVar");
        assertThat(variable.getType()).isEqualTo(variableType);
        assertThat(variable.getValue()).isEqualTo(variableValue);
        assertThat(variable.getProcessInstanceId()).isEqualTo(userTask.getProcessInstanceId());
    }

    @ParameterizedTest
    @MethodSource("provideNotValidPrimitiveVariables")
    @DisplayName("ClassCastException thrown in complete task by id with invalid primitive variable type")
    void givenActiveTaskIdAndInvalidPrimitiveVariable_whenCompleteById_thenTaskNotCompletedAndVariableNotSet(String variableType, Object variableValue) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskVariable.bpmn")
                .startByKey("testUserTaskVariable");

        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "testUserTaskVariable").get(0);

        String taskId = userTask.getId();
        VariableInstanceData variableInstance = createVariableInstance("myInvalidVar", variableType, variableValue);

        //then: check task not completed and variable is not set for process instance
        assertThatThrownBy(() -> {
            userTaskService.completeTaskById(taskId, List.of(variableInstance));
        }).isInstanceOf(ClassCastException.class);

        HistoricUserTaskDto historyUserTask = camundaRestTestHelper.findHistoryUserTask(camunda7, taskId);
        assertThat(historyUserTask).isNotNull();
        assertThat(historyUserTask.getEndTime()).isNull();

        VariableInstanceDto variable = camundaRestTestHelper.getVariable(camunda7, "myInvalidVar");
        assertThat(variable).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"String", "Long", "Boolean", "Double", "Integer", "Short", "Date"})
    @DisplayName("Complete existing active task by id with null primitive variable")
    void givenActiveTaskIdAndNullPrimitiveVariable_whenCompleteById_thenTaskCompletedAndVariableSet(String variableType) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskVariable.bpmn")
                .startByKey("testUserTaskVariable");

        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "testUserTaskVariable").get(0);

        String taskId = userTask.getId();
        VariableInstanceData variableInstance = createVariableInstance("myNullVar", variableType, null);

        //when
        userTaskService.completeTaskById(taskId, List.of(variableInstance));

        //then: check task completed and variable is set for process instance
        HistoricUserTaskDto historyUserTask = camundaRestTestHelper.findHistoryUserTask(camunda7, taskId);
        assertThat(historyUserTask).isNotNull();
        assertThat(historyUserTask.getEndTime()).isNotNull();
        assertThat(historyUserTask.getTaskDefinitionKey()).isEqualTo("firstUserTask");

        VariableInstanceDto variable = camundaRestTestHelper.getVariable(camunda7, "myNullVar");
        assertThat(variable).isNotNull();
        assertThat(variable.getName()).isEqualTo("myNullVar");
        assertThat(variable.getType()).isEqualTo(variableType);
        assertThat(variable.getValue()).isNull();
        assertThat(variable.getProcessInstanceId()).isEqualTo(userTask.getProcessInstanceId());
    }

    @Test
    @DisplayName("Complete existing active task by id with Short type variable")
    void givenActiveTaskIdAndNotNullShortVariable_whenCompleteById_thenTaskCompletedAndVariableSet() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskVariable.bpmn")
                .startByKey("testUserTaskVariable");

        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "testUserTaskVariable").get(0);

        String taskId = userTask.getId();
        VariableInstanceData variableInstance = createVariableInstance("myVar", "Short", Short.MAX_VALUE);

        //when
        userTaskService.completeTaskById(taskId, List.of(variableInstance));

        //then: check task completed and variable is set for process instance
        HistoricUserTaskDto historyUserTask = camundaRestTestHelper.findHistoryUserTask(camunda7, taskId);
        assertThat(historyUserTask).isNotNull();
        assertThat(historyUserTask.getEndTime()).isNotNull();
        assertThat(historyUserTask.getTaskDefinitionKey()).isEqualTo("firstUserTask");

        VariableInstanceDto variable = camundaRestTestHelper.getVariable(camunda7, "myVar");
        assertThat(variable).isNotNull();
        assertThat(variable.getName()).isEqualTo("myVar");
        assertThat(variable.getType()).isEqualTo("Short");
        assertThat(variable.getValue()).isNotNull()
                .asString()
                .isEqualTo(String.valueOf(Short.MAX_VALUE));
        assertThat(variable.getProcessInstanceId()).isEqualTo(userTask.getProcessInstanceId());
    }

    @Test
    @DisplayName("Complete existing active task by id with Date type variable")
    void givenActiveTaskIdAndNotNullDateVariable_whenCompleteById_thenTaskCompletedAndVariableSet() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskVariable.bpmn")
                .startByKey("testUserTaskVariable");

        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "testUserTaskVariable").get(0);

        String taskId = userTask.getId();
        Instant date = LocalDateTime.of(2025, 1, 12, 12, 0, 0).toInstant(ZoneOffset.UTC);
        VariableInstanceData variableInstance = createVariableInstance("myDateVar", "Date", Date.from(date));

        //when
        userTaskService.completeTaskById(taskId, List.of(variableInstance));

        //then: check task completed and variable is set for process instance
        HistoricUserTaskDto historyUserTask = camundaRestTestHelper.findHistoryUserTask(camunda7, taskId);
        assertThat(historyUserTask).isNotNull();
        assertThat(historyUserTask.getEndTime()).isNotNull();
        assertThat(historyUserTask.getTaskDefinitionKey()).isEqualTo("firstUserTask");

        VariableInstanceDto variable = camundaRestTestHelper.getVariable(camunda7, "myDateVar");
        assertThat(variable).isNotNull();
        assertThat(variable.getName()).isEqualTo("myDateVar");
        assertThat(variable.getType()).isEqualTo("Date");
        assertThat(variable.getValue()).isEqualTo("2025-01-12T12:00:00.000+0000");
        assertThat(variable.getProcessInstanceId()).isEqualTo(userTask.getProcessInstanceId());
    }

    @Test
    @DisplayName("RemoteEngineException is thrown if complete existing suspended task")
    void givenSuspendedTask_whenCompleteById_thenRemoteEngineExceptionIsThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");

        RuntimeProcessInstanceDto processInstance = camundaRestTestHelper.getRuntimeInstancesByKey(camunda7, "userTaskWithoutAssignee").get(0);
        camundaRestTestHelper.suspendInstanceById(camunda7, processInstance.getId());

        RuntimeUserTaskDto suspendedUserTask = camundaRestTestHelper.findRuntimeUserTasks(camunda7, processInstance.getId()).get(0);
        String suspendedTaskId = suspendedUserTask.getId();

        //when and then
        assertThatThrownBy(() -> userTaskService.completeTaskById(suspendedTaskId, List.of()))
                .isInstanceOf(RemoteProcessEngineException.class)
                .hasMessageContaining("task with id '%s' is suspended", suspendedTaskId);
    }

    @Test
    @DisplayName("EngineConnectionFailedException is thrown when complete task by id and engine is not available")
    void givenActiveTaskIdAndNotAvailableEngine_whenCompleteById_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");

        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithoutAssignee").get(0);

        String taskId = userTask.getId();

        camunda7.stop();

        //when and then
        assertThatThrownBy(() ->  userTaskService.completeTaskById(taskId, List.of()))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    private static Stream<Arguments> provideNonNullPrimitiveVariables() {
        return Stream.of(
                Arguments.of("String", "My string var"),
                Arguments.of("Integer", Integer.MAX_VALUE),
                Arguments.of("Long", Long.MAX_VALUE),
                Arguments.of("Boolean", true),
                Arguments.of("Double", 15.5d)
        );
    }

    private static Stream<Arguments> provideNotValidPrimitiveVariables() {
        return Stream.of(
                Arguments.of("String", 123),
                Arguments.of("Integer", "Invalid int"),
                Arguments.of("Long", "Invalid long"),
                Arguments.of("Boolean", "Invalid boolean"),
                Arguments.of("Double", "Invalid double")
        );
    }

    private VariableInstanceData createVariableInstance(String name, String type, @Nullable Object value) {
        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName(name);
        variableInstanceData.setType(type);
        variableInstanceData.setValue(value);

        return variableInstanceData;
    }
}
