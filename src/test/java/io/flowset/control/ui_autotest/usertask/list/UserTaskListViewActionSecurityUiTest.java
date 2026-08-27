/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.usertask.list;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskCompletePermissionRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskListNoLinkedDetailViewRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskNoProcessDefinitionViewRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskNoProcessInstanceViewRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskReassignPermissionRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.usertask.AllTasksListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.PROCESS_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.PROCESS_DEFINITION_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.PROCESS_INSTANCE_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.PROCESS_INSTANCE_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.TASK_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on User task list view")
@Tag("security")
public class UserTaskListViewActionSecurityUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDownUsers() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME like 'test-user%'");
        jdbcTemplate.update(
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in " +
                        "(select ID from SEC_RESOURCE_ROLE where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("completeVisibilityOnListViewSource")
    @DisplayName("Complete action visibility on User task list view")
    void givenExistingUserTask_whenOpenUserTaskList_thenCompleteActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-task-complete", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAs("test-user-user-task-complete", "password");

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        GridContextMenu contextMenu = listView.openTasksGridContextMenu();
        if (expectedVisible) {
            listView.getCompleteTaskBtn().shouldBe(VISIBLE);
            contextMenu.find(text("Complete")).shouldBe(VISIBLE);
        } else {
            listView.getCompleteTaskBtn().shouldNotBe(VISIBLE);
            contextMenu.find(text("Complete")).shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("reassignVisibilityOnListViewSource")
    @DisplayName("Reassign action visibility on User task list view")
    void givenExistingUserTask_whenOpenUserTaskList_thenReassignActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-task-reassign", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAs("test-user-user-task-reassign", "password");

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        GridContextMenu contextMenu = listView.openTasksGridContextMenu();
        if (expectedVisible) {
            listView.getReassignTaskBtn().shouldBe(VISIBLE);
            contextMenu.find(text("Reassign")).shouldBe(VISIBLE);
        } else {
            listView.getReassignTaskBtn().shouldNotBe(VISIBLE);
            contextMenu.find(text("Reassign")).shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilityOnListViewSource")
    @DisplayName("Task ID link availability")
    void givenExistingUserTask_whenOpenUserTaskList_thenTaskIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser("test-user-user-task-detail-link", "password", roleClass);

        MainView mainView = loginAs("test-user-user-task-detail-link", "password");

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        if (expectedAvailable) {
            listView.getRowByTaskId(taskId)
                    .getCellByIndex(TASK_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByTaskId(taskId)
                    .getCellByIndex(TASK_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processInstanceLinkAvailabilityOnListViewSource")
    @DisplayName("Process instance navigation link availability")
    void givenExistingUserTask_whenOpenUserTaskList_thenProcessInstanceLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser("test-user-user-task-process-instance-link", "password", roleClass);

        MainView mainView = loginAs("test-user-user-task-process-instance-link", "password");

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        if (expectedAvailable) {
            listView.getRowByTaskId(taskId)
                    .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_INSTANCE_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByTaskId(taskId)
                    .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_INSTANCE_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processLinkAvailabilityOnListViewSource")
    @DisplayName("Process navigation link availability")
    void givenExistingUserTask_whenOpenUserTaskList_thenProcessLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser("test-user-user-task-process-link", "password", roleClass);

        MainView mainView = loginAs("test-user-user-task-process-link", "password");

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        if (expectedAvailable) {
            listView.getRowByTaskId(taskId)
                    .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByTaskId(taskId)
                    .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without User task list view policy")
    void givenUserWithoutUserTaskListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-user-task-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-user-task-no-list-view-policy", "password");

        // when
        open("/bpm/user-tasks");

        // then
        $j(AllTasksListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/user-tasks'"));
    }

    private static Stream<Arguments> completeVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without complete permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with complete permission", TestUserTaskCompletePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> reassignVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without reassign permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with reassign permission", TestUserTaskReassignPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> processInstanceLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessInstanceData view permission", TestUserTaskNoProcessInstanceViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without process instance detail view access",
                                TestUserTaskListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with ProcessInstanceData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> processLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessDefinitionData view permission", TestUserTaskNoProcessDefinitionViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without process definition detail view access",
                                TestUserTaskListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with ProcessDefinitionData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without user task detail view access", TestUserTaskListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with user task detail view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
