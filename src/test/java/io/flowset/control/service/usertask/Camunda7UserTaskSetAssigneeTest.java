/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.usertask;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7UserTaskSetAssigneeTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    UserTaskService userTaskService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Set a new assignee for active user task if previous assignee is not set")
    void givenActiveUserTaskWithoutAssignee_whenSetAssignee_thenTaskAssigneeUpdated() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");

        RuntimeUserTaskDto sourceUserTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithoutAssignee").get(0);

        //when
        userTaskService.setAssignee(sourceUserTask.getId(), "manager");

        //then
        RuntimeUserTaskDto foundTask = camundaRestTestHelper.findRuntimeUserTask(camunda7, sourceUserTask.getId());
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getId()).isEqualTo(sourceUserTask.getId());
        assertThat(foundTask.getAssignee()).isNotEqualTo(sourceUserTask.getAssignee());
        assertThat(foundTask.getAssignee()).isEqualTo("manager");
    }

    @Test
    @DisplayName("Set a new assignee for active user task if previous assignee is set")
    void givenActiveUserTaskWithAssignee_whenSetAssignee_thenTaskAssigneeUpdated() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        RuntimeUserTaskDto sourceUserTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithAssignee").get(0);

        //when
        userTaskService.setAssignee(sourceUserTask.getId(), "manager");

        //then
        RuntimeUserTaskDto foundTask = camundaRestTestHelper.findRuntimeUserTask(camunda7, sourceUserTask.getId());
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getId()).isEqualTo(sourceUserTask.getId());
        assertThat(foundTask.getAssignee()).isNotEqualTo(sourceUserTask.getAssignee());
        assertThat(foundTask.getAssignee()).isEqualTo("manager");
    }

    @Test
    @DisplayName("Set a new assignee for active user task if previous assignee is the same")
    void givenActiveUserTaskWithAssignee_whenSetTheSameAssignee_thenTaskAssigneeUpdated() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        RuntimeUserTaskDto sourceUserTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithAssignee").get(0);

        //when
        userTaskService.setAssignee(sourceUserTask.getId(), "admin");

        //then
        RuntimeUserTaskDto foundTask = camundaRestTestHelper.findRuntimeUserTask(camunda7, sourceUserTask.getId());
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getId()).isEqualTo(sourceUserTask.getId());
        assertThat(foundTask.getAssignee()).isEqualTo(sourceUserTask.getAssignee());
        assertThat(foundTask.getAssignee()).isEqualTo("admin");
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when set task assignee if engine is not available")
    void givenActiveTaskAndNotAvailableEngine_whenSetAsignee_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        RuntimeUserTaskDto sourceUserTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithAssignee").get(0);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> userTaskService.setAssignee(sourceUserTask.getId(), "admin"))
                .isInstanceOf(EngineConnectionFailedException.class);
    }
}
