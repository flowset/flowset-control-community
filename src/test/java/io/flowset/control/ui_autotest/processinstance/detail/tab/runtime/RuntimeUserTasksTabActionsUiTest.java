/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeUserTasksTabFragment;
import io.flowset.control.test_support.ui.view.usertask.TaskReassignDialog;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.anyBodyRowHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeUserTasksTabFragment.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the User tasks tab (Runtime tab) in Process instance detail view")
public class RuntimeUserTasksTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Reassign action availability on User tasks tab in process instance detail view")
    void givenExistingUserTask_whenOpenUserTasksTab_thenReassignActionAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);
        String userTaskId = dataManager.getUserTasksByKey("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeUserTasksTab();

        // then
        userTasksTab.getReassignButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = userTasksTab.openTasksGridContextMenu();
        gridContextMenu.find(text("Reassign"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        userTasksTab.selectRowByTaskId(userTaskId);

        userTasksTab.getReassignButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        userTasksTab.openTasksGridContextMenu()
                .find(text("Reassign"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }


    @Test
    @DisplayName("Reassign action: assignee is updated after confirmation")
    void givenExistingUserTask_whenReassignConfirmed_thenAssigneeUpdated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeUserTasksTab();

        userTasksTab.selectRowByTaskName("Approve vacation");

        userTasksTab.getReassignButton()
                .shouldBe(ENABLED).click();

        TaskReassignDialog dialog = $j(TaskReassignDialog.class).exists()
                .displayed();
        dialog.getNewAssigneeField().setValue("manager");
        dialog.getOkBtn().click();

        // then
        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(anyBodyRowHaveCellText(NAME_COLUMN_INDEX, "Approve vacation"))
                .shouldHave(anyBodyRowHaveCellText(ASSIGNEE_COLUMN_INDEX, "manager"));
    }

    @Test
    @DisplayName("Reassign action: assignee is not updated after cancellation")
    void givenExistingUserTask_whenReassignCancelled_thenAssigneeUnchanged() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeUserTasksTab();

        userTasksTab.selectRowByTaskName("Approve vacation");

        userTasksTab.getReassignButton().click();

        TaskReassignDialog dialog = $j(TaskReassignDialog.class).exists();
        dialog.shouldBe(VISIBLE);
        dialog.getNewAssigneeField().setValue("manager");
        dialog.getCancelBtn().click();

        // then
        userTasksTab.getRuntimeUserTasksGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(anyBodyRowHaveCellText(NAME_COLUMN_INDEX, "Approve vacation"))
                .shouldHave(anyBodyRowHaveCellText(ASSIGNEE_COLUMN_INDEX, "admin"));
    }

    @Test
    @DisplayName("View action in Id column opens User task detail dialog")
    void givenExistingUserTask_whenViewActionInvoked_thenUserTaskDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);
        String taskId = camundaRestTestHelper.getUserTasksByInstanceIds(camunda7, instanceId).get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeUserTasksTab();

        userTasksTab.getRowByTaskId(taskId)
                .getCellByIndex(TASK_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        UserTaskDataDetailDialog dialog = $j(UserTaskDataDetailDialog.class)
                .exists()
                .displayed();

        dialog.getTaskIdField().shouldHave(value(taskId));
    }

    @Test
    @DisplayName("Double-clicking a user task row opens User task detail dialog")
    void givenExistingUserTask_whenRowDoubleClicked_thenUserTaskDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);
        String taskId = camundaRestTestHelper.getUserTasksByInstanceIds(camunda7, instanceId).get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeUserTasksTab();

        userTasksTab.getRowByTaskId(taskId)
                .getCellByIndex(CREATE_TIME_COLUMN_INDEX)
                .getDelegate()
                .doubleClick();

        // then
        UserTaskDataDetailDialog dialog = $j(UserTaskDataDetailDialog.class)
                .exists()
                .displayed();

        dialog.getTaskIdField().shouldHave(value(taskId));
    }

    @Test
    @DisplayName("List of available actions on User tasks tab")
    void givenExistingUserTask_whenOpenDetailView_thenAllUserTasksActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeUserTasksTab();

        // then
        userTasksTab.getReassignButton().shouldBe(VISIBLE);

        userTasksTab.openTasksGridContextMenu()
                .shouldHave(visibleItems("Reassign"));
    }
}
