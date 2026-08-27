/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.history;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryUserTasksTabFragment;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryUserTasksTabFragment.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryUserTasksTabFragment.TASK_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.visibleItemsCount;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the User tasks tab (History tab) in Process instance detail view")
public class HistoryUserTasksTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("No actions available for the User tasks data grid")
    void givenExistingHistoricUserTask_whenOpenHistoryUserTasksTab_thenNoGridActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryUserTasksTab();

        userTasksTab.selectRowByTaskDefinitionKey("approveVacationTask");

        // then
        userTasksTab.openTasksGridContextMenu()
                .shouldHave(visibleItemsCount(0));
    }

    @Test
    @DisplayName("Task ID link in data grid opens User task detail dialog")
    void givenExistingHistoricUserTask_whenTaskIdLinkClicked_thenUserTaskDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryUserTasksTab();

        userTasksTab.getRowByTaskDefinitionKey("approveVacationTask")
                .getCellByIndex(TASK_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        UserTaskDataDetailDialog dialog = $j(UserTaskDataDetailDialog.class).exists()
                .displayed();
        dialog.getTaskDefinitionKeyField().shouldHave(value("approveVacationTask"));
        dialog.getNameField().shouldHave(value("Approve vacation"));
        dialog.getAssigneeField().shouldHave(value("admin"));
    }

    @Test
    @DisplayName("Double-clicking a user task row opens User task detail dialog")
    void givenExistingHistoricUserTask_whenRowDoubleClicked_thenUserTaskDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);
        String userTaskId = dataManager.getUserTasksByKey("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryUserTasksTab();

        userTasksTab.getRowByTaskId(userTaskId)
                .getDelegate()
                .doubleClick();

        // then
        UserTaskDataDetailDialog dialog = $j(UserTaskDataDetailDialog.class).exists()
                .displayed();
        dialog.getTaskIdField().shouldHave(value(userTaskId));
    }
}
