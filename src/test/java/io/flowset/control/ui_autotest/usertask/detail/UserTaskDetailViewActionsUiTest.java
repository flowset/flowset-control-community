/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.usertask.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.usertask.TaskCompleteDialog;
import io.flowset.control.test_support.ui.view.usertask.TaskReassignDialog;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on User task detail view")
public class UserTaskDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("All actions are visible for active task")
    void givenActiveUserTask_whenOpenDetailDialog_thenAllActionsVisible() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // then
        dialog.getReassignBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
        dialog.getCompleteBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
        dialog.getCloseBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Reassign and Complete buttons are hidden for completed task")
    void givenCompletedUserTask_whenOpenDetailDialog_thenReassignAndCompleteHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        String taskId = dataManager.getUserTasksByKey("userTaskWithAssignee").get(0);
        camundaRestTestHelper.completeTaskById(camunda7, taskId);

        String instanceId = dataManager.getStartedInstances("userTaskWithAssignee").get(0);

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog dialog = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryUserTasksTab()
                .openDetailView(taskId);

        // then
        dialog.getReassignBtn().shouldNotBe(VISIBLE);
        dialog.getCompleteBtn().shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Reassign and Complete buttons are hidden for suspended task")
    void givenSuspendedUserTask_whenOpenDetailDialog_thenReassignAndCompleteHidden() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true)
                .getUserTasksByKey("vacation_approval")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // then
        dialog.getReassignBtn()
                .shouldNotBe(VISIBLE);
        dialog.getCompleteBtn()
                .shouldNotBe(VISIBLE);
        dialog.getCloseBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Close button closes the detail dialog")
    void givenActiveUserTask_whenCloseClickedOnDetailView_thenDetailViewClosed() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        UserTaskDataDetailDialog dialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        dialog.getCloseBtn().click();

        // then
        dialog.shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Reassign action: detail view is closed after confirmation")
    void givenActiveUserTask_whenReassignConfirmed_thenDetailViewClosed() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee")
                .getUserTasksByKey("userTaskWithAssignee")
                .get(0);

        MainView mainView = loginAsAdmin();
        UserTaskDataDetailDialog detailDialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // when
        detailDialog.getReassignBtn().click();

        TaskReassignDialog reassignDialog = $j(TaskReassignDialog.class)
                .exists()
                .displayed();

        reassignDialog.getNewAssigneeField().setValue("manager");
        reassignDialog.getOkBtn().click();

        // then
        reassignDialog.shouldNotBe(EXIST);
        detailDialog.shouldNotBe(EXIST);
    }

    @Test
    @DisplayName("Reassign action: detail view is not closed after cancellation")
    void givenActiveUserTask_whenReassignCancelled_thenDetailViewNotClosed() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        MainView mainView = loginAsAdmin();
        UserTaskDataDetailDialog detailDialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // when
        detailDialog.getReassignBtn().click();

        TaskReassignDialog reassignDialog = $j(TaskReassignDialog.class)
                .exists()
                .displayed();
        reassignDialog.getNewAssigneeField().setValue("manager");
        reassignDialog.getCancelBtn().click();

        // then
        reassignDialog.shouldNotBe(EXIST);
        detailDialog.exists()
                .displayed();
    }

    @Test
    @DisplayName("Complete action: detail view is closed after confirmation")
    void givenActiveUserTask_whenCompleteConfirmed_thenDetailViewClosed() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee")
                .getUserTasksByKey("userTaskWithAssignee")
                .get(0);

        MainView mainView = loginAsAdmin();
        UserTaskDataDetailDialog detailDialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // when
        detailDialog.getCompleteBtn().click();

        TaskCompleteDialog completeDialog = $j(TaskCompleteDialog.class)
                .exists()
                .displayed();
        completeDialog.getCompleteBtn().click();

        // then
        completeDialog.shouldNotBe(EXIST);
        detailDialog.shouldNotBe(EXIST);
    }

    @Test
    @DisplayName("Complete action: detail view is not closed after cancellation")
    void givenActiveUserTask_whenCompleteCancelled_thenDetailViewNotClosed() {
        // given
        String taskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee")
                .getUserTasksByKey("userTaskWithAssignee")
                .get(0);

        MainView mainView = loginAsAdmin();
        UserTaskDataDetailDialog detailDialog = mainView.openUserTaskListView()
                .openDetailView(taskId);

        // when
        detailDialog.getCompleteBtn().click();

        TaskCompleteDialog completeDialog = $j(TaskCompleteDialog.class)
                .exists()
                .displayed();
        completeDialog.getCancelBtn().click();

        // then
        completeDialog.shouldNotBe(EXIST);
        detailDialog.exists()
                .displayed();
    }
}
