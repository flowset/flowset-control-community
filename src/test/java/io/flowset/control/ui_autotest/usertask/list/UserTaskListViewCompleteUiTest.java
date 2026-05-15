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
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.usertask.AllTasksListView;
import io.flowset.control.test_support.ui.view.usertask.BulkTaskCompleteDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Complete task action on User tasks list view")
public class UserTaskListViewCompleteUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Complete action: selected user tasks are removed from the data grid after confirmation")
    void givenActiveUserTask_whenCompleteTaskConfirmed_thenTasksRemovedFromDataGrid() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUserTaskWithAssignee.bpmn")
                .startByKey("userTaskWithAssignee");

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();
        listView.getTasksDataGrid().clickSelectAll();
        listView.getCompleteTaskBtn().click();

        BulkTaskCompleteDialog taskCompleteDialog = $j(BulkTaskCompleteDialog.class);
        taskCompleteDialog.exists()
                .displayed()
                .getCompleteBtn().click();

        // then
        taskCompleteDialog.shouldNotBe(EXIST);

        listView.waitUntilDataLoading()
                .getTasksDataGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Complete action: selected user tasks are not changed after cancellation")
    void givenActiveUserTask_whenCompleteTaskCancelled_thenTasksNotRemovedFromDataGrid() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();
        listView.getTasksDataGrid().clickSelectAll();
        listView.getCompleteTaskBtn().click();

        $j(BulkTaskCompleteDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getTasksDataGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Complete action is disabled for selected suspended task")
    void givenSuspendedUserTask_whenSelectGridRow_thenCompleteActionDisabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .suspendByKey("vacation_approval", true);

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();
        listView.getTasksDataGrid().clickSelectAll();

        // then
        listView.getCompleteTaskBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openTasksGridContextMenu()
                .find(text("Complete"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Complete task action is enabled for selected active task")
    void givenActiveUserTask_whenSelectGridRow_thenCompleteTaskActionEnabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        listView.getCompleteTaskBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openTasksGridContextMenu();
        gridContextMenu.find(text("Complete"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getTasksDataGrid().clickSelectAll();

        listView.getCompleteTaskBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openTasksGridContextMenu()
                .find(text("Complete"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

}
