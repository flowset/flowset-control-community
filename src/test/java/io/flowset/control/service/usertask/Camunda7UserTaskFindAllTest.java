/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.usertask;

import io.flowset.control.entity.UserTaskData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeUserTaskDto;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7UserTaskFindAllTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    UserTaskService userTaskService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("EngineConnectionFailedException thrown when load runtime user tasks if selected engine is not available")
    void givenEmptyLoadContextAndNotAvailableEngine_whenFindRuntimeTasks_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee")
                .startByKey("userTaskWithAssignee");

        camunda7.stop();

        UserTaskLoadContext userTaskLoadContext = new UserTaskLoadContext();

        //when and then
        assertThatThrownBy(() -> userTaskService.findRuntimeTasks(userTaskLoadContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Empty list with tasks is returned if no selected engine")
    void givenEmptyLoadContextAndNoSelectedEngine_whenFindRuntimeTasks_thenNoUserTasksReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee")
                .startByKey("userTaskWithAssignee");

        resetSelectedEngine();

        UserTaskLoadContext userTaskLoadContext = new UserTaskLoadContext();

        //when
        List<UserTaskData> foundUserTasks = userTaskService.findRuntimeTasks(userTaskLoadContext);

        //then
        assertThat(foundUserTasks).isEmpty();
    }

    @Test
    @DisplayName("Completed task is not returned in runtime tasks")
    void givenCompletedTaskAndEmptyContext_whenFindRuntimeTasks_thenOnlyRuntimeTasksReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");

        RuntimeUserTaskDto task = camundaRestTestHelper.getRuntimeUserTasks(camunda7).get(0);

        camundaRestTestHelper.completeTaskById(camunda7, task.getId());

        UserTaskLoadContext userTaskLoadContext = new UserTaskLoadContext();

        //when
        List<UserTaskData> foundRuntimeTasks = userTaskService.findRuntimeTasks(userTaskLoadContext);

        //then
        assertThat(foundRuntimeTasks).isEmpty();
    }

    @Test
    @DisplayName("All runtime user tasks are loaded with empty load context")
    void givenEmptyLoadContext_whenFindRuntimeTasks_thenAllUserTasksReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee")
                .startByKey("userTaskWithAssignee");

        UserTaskLoadContext userTaskLoadContext = new UserTaskLoadContext();

        //when
        List<UserTaskData> foundUserTasks = userTaskService.findRuntimeTasks(userTaskLoadContext);

        //then
        assertThat(foundUserTasks).isNotNull()
                .hasSize(2)
                .extracting(UserTaskData::getTaskDefinitionKey, UserTaskData::getName,
                        UserTaskData::getProcessDefinitionKey, UserTaskData::getAssignee)
                .contains(tuple("taskWithAssignee", "Task with assignee", null, "admin"),
                        tuple("taskWithoutAssignee", "Task without assignee", null, null));
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Page with runtime user tasks is loaded")
    void givenLoadContextWithValidPaginationData_whenFindRuntimeTasks_thenAllUserTasksReturned(int firstResult, int maxResults, int expectedCount) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee", 2)
                .startByKey("userTaskWithAssignee");

        UserTaskLoadContext userTaskLoadContext = new UserTaskLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<UserTaskData> foundUserTasks = userTaskService.findRuntimeTasks(userTaskLoadContext);

        //then
        assertThat(foundUserTasks).isNotNull()
                .hasSize(expectedCount);
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Page with historic user tasks is loaded")
    void givenLoadContextWithValidPaginationData_whenFindHistoricTasks_thenAllUserTasksReturned(int firstResult, int maxResults, int expectedCount) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee", 2)
                .startByKey("userTaskWithAssignee");

        UserTaskLoadContext userTaskLoadContext = new UserTaskLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<UserTaskData> foundUserTasks = userTaskService.findHistoricTasks(userTaskLoadContext);

        //then
        assertThat(foundUserTasks).isNotNull()
                .hasSize(expectedCount);
    }

    static Stream<Arguments> provideValidPaginationData() {
        return Stream.of(
                Arguments.of(0, 2, 2),
                Arguments.of(2, 4, 1)
        );
    }
}
