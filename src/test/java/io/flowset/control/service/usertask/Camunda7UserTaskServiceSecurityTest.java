/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.usertask;

import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.HistoricUserTaskDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeUserTaskDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskCompletePermissionRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskReassignPermissionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRunningEngine
public class Camunda7UserTaskServiceSecurityTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    UserTaskService userTaskService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Complete user task by id when user has complete permission")
    @WithTestUser(username = "test-user-secured-user-task-complete",
            roles = TestUserTaskCompletePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-user-task-complete")
    void givenUserWithUserTaskCompletePermission_whenCompleteTaskById_thenTaskCompleted() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");
        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithoutAssignee")
                .get(0);

        //when
        userTaskService.completeTaskById(userTask.getId(), List.of());

        //then
        assertThat(camundaRestTestHelper.runtimeUserTaskExists(camunda7, userTask.getId())).isFalse();
        HistoricUserTaskDto historyUserTask = camundaRestTestHelper.findHistoryUserTask(camunda7, userTask.getId());
        assertThat(historyUserTask).isNotNull();
        assertThat(historyUserTask.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("Reassign user task when user has reassign permission")
    @WithTestUser(username = "test-user-secured-user-task-reassign",
            roles = TestUserTaskReassignPermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-user-task-reassign")
    void givenUserWithUserTaskReassignPermission_whenSetAssignee_thenAssigneeChanged() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithoutAssignee.bpmn")
                .startByKey("userTaskWithoutAssignee");
        RuntimeUserTaskDto userTask = camundaRestTestHelper.findRuntimeUserTasksByProcessKey(camunda7, "userTaskWithoutAssignee")
                .get(0);

        //when
        userTaskService.setAssignee(userTask.getId(), "manager");

        //then
        RuntimeUserTaskDto updatedTask = camundaRestTestHelper.findRuntimeUserTask(camunda7, userTask.getId());
        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getAssignee()).isEqualTo("manager");
    }
}
