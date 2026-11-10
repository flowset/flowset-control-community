/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.usertask;

import io.flowset.control.entity.UserTaskData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class NoopUserTaskServiceTest extends AbstractIntegrationTest {

    @Autowired
    UserTaskService userTaskService;

    @Test
    @DisplayName("Empty list of runtime tasks returned when no engine exists")
    void givenNoEngine_whenFindRuntimeTasks_thenEmptyListReturned() {
        //when
        List<UserTaskData> tasks = userTaskService.findRuntimeTasks(new UserTaskLoadContext());

        //then
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("Empty list of historic tasks returned when no engine exists")
    void givenNoEngine_whenFindHistoricTasks_thenEmptyListReturned() {
        //when
        List<UserTaskData> tasks = userTaskService.findHistoricTasks(new UserTaskLoadContext());

        //then
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as runtime tasks count when no engine exists")
    void givenNoEngine_whenGetRuntimeTasksCount_thenZeroReturned() {
        //when
        long count = userTaskService.getRuntimeTasksCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as historic tasks count when no engine exists")
    void givenNoEngine_whenGetHistoryTasksCount_thenZeroReturned() {
        //when
        long count = userTaskService.getHistoryTasksCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for user task when no engine exists")
    void givenNoEngine_whenFindTaskById_thenNullReturned() {
        //when
        UserTaskData task = userTaskService.findTaskById("taskId");

        //then
        assertThat(task).isNull();
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when set assignee if no engine exists")
    void givenNoEngine_whenSetAssignee_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> userTaskService.setAssignee("taskId", "admin"))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when complete task if no engine exists")
    void givenNoEngine_whenCompleteTaskById_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> userTaskService.completeTaskById("taskId", List.of()))
                .isInstanceOf(EngineNotSelectedException.class);
    }
}
