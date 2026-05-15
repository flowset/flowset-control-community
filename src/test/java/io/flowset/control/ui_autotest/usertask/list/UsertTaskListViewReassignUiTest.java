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
import io.flowset.control.test_support.ui.view.usertask.TaskReassignDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.ASSIGNEE_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.NAME_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Reassign task action on User tasks list view")
public class UsertTaskListViewReassignUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Reassign action: assignee is updated in data grid after confirmation")
    void givenActiveUserTask_whenReassignConfirmed_thenAssigneeUpdatedInGrid() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();
        listView.getTasksDataGrid().clickSelectAll();
        listView.getReassignTaskBtn().click();

        TaskReassignDialog dialog = $j(TaskReassignDialog.class)
                .exists()
                .displayed();
        dialog.getNewAssigneeField().setValue("manager");
        dialog.getOkBtn().click();

        // then
        listView.getTasksDataGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ASSIGNEE_COLUMN_INDEX, "manager"));
    }

    @Test
    @DisplayName("Reassign action: assignee is not changed in data grid after cancellation")
    void givenActiveUserTask_whenReassignCancelled_thenAssigneeNotChangedInGrid() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();
        listView.getTasksDataGrid().clickSelectAll();
        listView.getReassignTaskBtn().click();

        TaskReassignDialog dialog = $j(TaskReassignDialog.class)
                .exists()
                .displayed();
        dialog.getNewAssigneeField().setValue("manager");
        dialog.getCancelBtn().click();

        // then
        listView.getTasksDataGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(NAME_COLUMN_INDEX, "Approve vacation"))
                .shouldHave(allBodyRowsHaveCellText(ASSIGNEE_COLUMN_INDEX, "admin"));
    }

    @Test
    @DisplayName("Reassign task action is enabled for selected active task")
    void givenActiveUserTask_whenSelectGridRow_thenReassignTaskActionEnabled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        listView.getReassignTaskBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openTasksGridContextMenu();
        gridContextMenu.find(text("Reassign"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getTasksDataGrid().clickSelectAll();

        listView.getReassignTaskBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openTasksGridContextMenu()
                .find(text("Reassign"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Reassign action is disabled for suspended selected task")
    void givenSuspendedUserTask_whenSelectGridRow_thenReassignTaskActionDisabled() {
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
        listView.getReassignTaskBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openTasksGridContextMenu()
                .find(text("Reassign"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }
}
