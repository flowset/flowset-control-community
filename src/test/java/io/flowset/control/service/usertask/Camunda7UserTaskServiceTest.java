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
import io.flowset.control.test_support.camunda7.dto.response.RuntimeProcessInstanceDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeUserTaskDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7UserTaskServiceTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    UserTaskService userTaskService;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private CamundaRestTestHelper camundaRestTestHelper;


    @Test
    @DisplayName("Count of all runtime tasks is returned if filter is null")
    void givenActiveTasksAndNullFilter_whenGetRuntimeTasksCount_thenAllRuntimeTasksCountReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee")
                .startByKey("userTaskWithAssignee");

        //when
        long runtimeTasksCount = userTaskService.getRuntimeTasksCount(null);

        //then
        assertThat(runtimeTasksCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Count of all historic tasks is returned if filter is null")
    void givenActiveAndCompletedTasksAndNullFilter_whenGetHistoricTasksCount_thenAllHistoricTasksCountReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee")
                .startByKey("userTaskWithAssignee");

        RuntimeUserTaskDto task = camundaRestTestHelper.getRuntimeUserTasks(camunda7).get(0);

        camundaRestTestHelper.completeTaskById(camunda7, task.getId());

        //when
        long historicTasksCount = userTaskService.getHistoryTasksCount(null);

        //then
        assertThat(historicTasksCount).isEqualTo(2);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get historic tasks count if engine is not available")
    void givenCompletedTasksAndNotAvailableEngine_whenGetHistoryTasksCount_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee")
                .startByKey("userTaskWithAssignee");

        RuntimeUserTaskDto task = camundaRestTestHelper.getRuntimeUserTasks(camunda7).get(0);

        camundaRestTestHelper.completeTaskById(camunda7, task.getId());

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> userTaskService.getHistoryTasksCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Count of runtime tasks only is returned")
    void givenCompletedTaskAndNullFilter_whenGetRuntimeTasksCount_thenOnlyRuntimeTasksCountReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");

        RuntimeUserTaskDto task = camundaRestTestHelper.getRuntimeUserTasks(camunda7).get(0);

        camundaRestTestHelper.completeTaskById(camunda7, task.getId());

        //when
        long runtimeTasksCount = userTaskService.getRuntimeTasksCount(null);

        //then
        assertThat(runtimeTasksCount).isEqualTo(0);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get runtime tasks count if engine is not available")
    void givenActiveTasksAndNotAvailableEngine_whenGetRuntimeTasksCount_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> userTaskService.getRuntimeTasksCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get active existing task by id")
    void givenActiveExistingTask_whenFindTaskById_thenTaskReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        RuntimeUserTaskDto sourceUserTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithAssignee").get(0);

        //when
        UserTaskData foundTask = userTaskService.findTaskById(sourceUserTask.getId());

        //then
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getTaskId()).isEqualTo(sourceUserTask.getId());
        assertThat(foundTask.getAssignee()).isEqualTo("admin");
        assertThat(foundTask.getTaskDefinitionKey()).isEqualTo("taskWithAssignee");
        assertThat(foundTask.getActivityInstanceId()).isNotNull();
        assertThat(foundTask.getDescription()).isEqualTo("Description for task with assignee");
        assertThat(foundTask.getCreateTime()).isNotNull();
        assertThat(foundTask.getEndTime()).isNull();
        assertThat(foundTask.getDueDate()).isNull();
        assertThat(foundTask.getName()).isEqualTo("Task with assignee");
        assertThat(foundTask.getProcessDefinitionKey()).isEqualTo("userTaskWithAssignee");
        assertThat(foundTask.getSuspended()).isNull();
    }

    @Test
    @DisplayName("Get suspended existing task by id")
    void givenSuspendedExistingTask_whenFindTaskById_thenTaskReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        RuntimeProcessInstanceDto instance = camundaRestTestHelper.getRuntimeInstancesByKey(camunda7, "userTaskWithAssignee").get(0);
        camundaRestTestHelper.suspendInstanceById(camunda7, instance.getId());

        RuntimeUserTaskDto sourceUserTask = camundaRestTestHelper.findRuntimeUserTasks(camunda7,  instance.getId()).get(0);

        //when
        UserTaskData foundTask = userTaskService.findTaskById(sourceUserTask.getId());

        //then
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getTaskId()).isEqualTo(sourceUserTask.getId());
        assertThat(foundTask.getAssignee()).isEqualTo("admin");
        assertThat(foundTask.getTaskDefinitionKey()).isEqualTo("taskWithAssignee");
        assertThat(foundTask.getActivityInstanceId()).isNotNull();
        assertThat(foundTask.getDescription()).isEqualTo("Description for task with assignee");
        assertThat(foundTask.getCreateTime()).isNotNull();
        assertThat(foundTask.getEndTime()).isNull();
        assertThat(foundTask.getDueDate()).isNull();
        assertThat(foundTask.getName()).isEqualTo("Task with assignee");
        assertThat(foundTask.getProcessDefinitionKey()).isEqualTo("userTaskWithAssignee");
        assertThat(foundTask.getSuspended()).isNull();
    }

    @Test
    @DisplayName("Get completed existing task by id")
    void givenCompletedExistingTask_whenFindTaskById_thenTaskReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        RuntimeUserTaskDto sourceUserTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithAssignee").get(0);
        camundaRestTestHelper.completeTaskById(camunda7, sourceUserTask.getId());

        //when
        UserTaskData foundTask = userTaskService.findTaskById(sourceUserTask.getId());

        //then
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getTaskId()).isEqualTo(sourceUserTask.getId());
        assertThat(foundTask.getAssignee()).isEqualTo("admin");
        assertThat(foundTask.getTaskDefinitionKey()).isEqualTo("taskWithAssignee");
        assertThat(foundTask.getActivityInstanceId()).isNotNull();
        assertThat(foundTask.getDescription()).isEqualTo("Description for task with assignee");
        assertThat(foundTask.getCreateTime()).isNotNull();
        assertThat(foundTask.getDuration()).isNotNull();
        assertThat(foundTask.getEndTime()).isNotNull();
        assertThat(foundTask.getDueDate()).isNull();
        assertThat(foundTask.getName()).isEqualTo("Task with assignee");
        assertThat(foundTask.getProcessDefinitionKey()).isEqualTo("userTaskWithAssignee");
        assertThat(foundTask.getSuspended()).isNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find task by id if engine is not available")
    void givenExistingTaskAndNotAvailableEngine_whenFindById_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        RuntimeUserTaskDto sourceUserTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithAssignee").get(0);

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> userTaskService.findTaskById(sourceUserTask.getId()))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

}
