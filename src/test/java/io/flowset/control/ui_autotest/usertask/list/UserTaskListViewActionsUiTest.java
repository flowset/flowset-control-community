/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.usertask.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.usertask.AllTasksListView;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on User tasks list view")
public class UserTaskListViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Refresh action availability")
    void givenLoggedInUser_whenOpenAllTasksView_thenRefreshActionAvailable() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        listView.getRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openTasksGridContextMenu()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action: data in data grid is updated")
    void givenLoggedInUser_whenClickRefresh_thenDataUpdated() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        listView.waitUntilDataLoading()
                .getTasksDataGrid()
                .shouldBe(emptyGrid);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        listView.getRefreshBtn().click();

        listView.getTasksDataGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Task ID link in row opens User task detail dialog")
    void givenExistingUserTask_whenTaskIdLinkClicked_thenUserTaskDetailDialogOpened() {
        // given
        String userTaskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        listView.getRowByTaskId(userTaskId)
                .getCellByIndex(TASK_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        UserTaskDataDetailDialog dialog = $j(UserTaskDataDetailDialog.class)
                .exists()
                .displayed();

        dialog.getTaskIdField().shouldHave(value(userTaskId));
    }

    @Test
    @DisplayName("Double-clicking a task row opens User task detail dialog")
    void givenExistingUserTask_whenRowDoubleClicked_thenUserTaskDetailDialogOpened() {
        // given
        String userTaskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getUserTasksByKey("vacation_approval")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        listView.getRowByTaskId(userTaskId)
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        // then
        UserTaskDataDetailDialog dialog = $j(UserTaskDataDetailDialog.class)
                .exists()
                .displayed();

        dialog.getTaskIdField().shouldHave(value(userTaskId));
    }

    @Test
    @DisplayName("Actions are visible in buttons panel and grid context menu")
    void givenLoggedInUser_whenOpenAllTasksView_thenActionsAreVisible() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        listView.getRefreshBtn().shouldBe(VISIBLE);
        listView.getCompleteTaskBtn().shouldBe(VISIBLE);
        listView.getReassignTaskBtn().shouldBe(VISIBLE);

        listView.openTasksGridContextMenu()
                .shouldHave(visibleItems("Refresh", "Complete", "Reassign"));
    }
}
