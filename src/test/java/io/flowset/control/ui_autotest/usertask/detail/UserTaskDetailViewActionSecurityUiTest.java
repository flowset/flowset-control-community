/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.usertask.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskCompletePermissionRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskNoProcessDefinitionViewRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskNoProcessInstanceViewRole;
import io.flowset.control.test_support.security.role.usertask.TestUserTaskReassignPermissionRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailView;
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
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on User task detail view")
@Tag("security")
public class UserTaskDetailViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("completeVisibilityOnDetailViewSource")
    @DisplayName("Complete action visibility on User task detail view")
    void givenExistingUserTask_whenOpenUserTaskDetail_thenCompleteActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser("test-user-user-task-detail-complete", "password", roleClass);

        MainView mainView = loginAs("test-user-user-task-detail-complete", "password");

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // then
        if (expectedVisible) {
            dialog.getCompleteBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            dialog.getCompleteBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("reassignVisibilityOnDetailViewSource")
    @DisplayName("Reassign action visibility on User task detail view")
    void givenExistingUserTask_whenOpenUserTaskDetail_thenReassignActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser("test-user-user-task-detail-reassign", "password", roleClass);

        MainView mainView = loginAs("test-user-user-task-detail-reassign", "password");

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // then
        if (expectedVisible) {
            dialog.getReassignBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            dialog.getReassignBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processInstanceButtonVisibilityOnDetailViewSource")
    @DisplayName("Process instance navigation button visibility on User task detail view")
    void givenExistingUserTask_whenOpenUserTaskDetail_thenProcessInstanceButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser("test-user-user-task-detail-process-instance", "password", roleClass);

        MainView mainView = loginAs("test-user-user-task-detail-process-instance", "password");

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // then
        if (expectedVisible) {
            dialog.getViewProcessInstanceBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            dialog.getViewProcessInstanceBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processButtonVisibilityOnDetailViewSource")
    @DisplayName("Process navigation button visibility on User task detail view")
    void givenExistingUserTask_whenOpenUserTaskDetail_thenProcessButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser("test-user-user-task-detail-process", "password", roleClass);

        MainView mainView = loginAs("test-user-user-task-detail-process", "password");

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // then
        if (expectedVisible) {
            dialog.getViewProcessDefinitionBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            dialog.getViewProcessDefinitionBtn().shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without User task detail view policy")
    void givenUserWithoutUserTaskDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser(
                "test-user-user-task-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-user-task-no-detail-view-policy", "password");

        // when
        open("/bpm/user-task/" + taskId);

        // then
        $j(UserTaskDataDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/user-task/" + taskId + "'"));
    }

    private static Stream<Arguments> completeVisibilityOnDetailViewSource() {
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

    private static Stream<Arguments> reassignVisibilityOnDetailViewSource() {
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

    private static Stream<Arguments> processInstanceButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessInstanceData view permission", TestUserTaskNoProcessInstanceViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ProcessInstanceData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> processButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessDefinitionData view permission", TestUserTaskNoProcessDefinitionViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ProcessDefinitionData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
