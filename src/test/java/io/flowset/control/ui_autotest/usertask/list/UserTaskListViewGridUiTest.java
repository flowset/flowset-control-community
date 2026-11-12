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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static io.flowset.control.test_support.ui.TagNames.GRID_COLUMN_SORTER_BY;
import static io.flowset.control.test_support.ui.condition.ControlCondition.bodyCellTextsExactly;
import static io.flowset.control.test_support.ui.condition.ControlCondition.columnNotSortable;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.PROCESS_DEFINITION_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.PROCESS_INSTANCE_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.usertask.AllTasksListView.TASK_DEFINITION_KEY_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Tasks Grid on User tasks list view")
public class UserTaskListViewGridUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Sort actions not available for Key, Process columns")
    void givenLoggedInUser_whenOpenAllTasksView_thenSortIconsNotAvailableOnColumns() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView()
                .waitUntilDataLoading();

        // then
        listView.getTasksDataGrid()
                .shouldHave(columnNotSortable(TASK_DEFINITION_KEY_COLUMN_INDEX))
                .shouldHave(columnNotSortable(PROCESS_DEFINITION_ID_COLUMN_INDEX));
    }

    @Test
    @DisplayName("User tasks are sorted by Process instance column after clicking on column header")
    void givenMultipleActiveUserTasks_whenSortByProcessInstanceColumn_thenSortedByProcessInstanceDisplayed() {
        // given
        List<String> sortedInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 3)
                .getStartedInstances("vacation_approval")
                .stream()
                .sorted()
                .toList();
        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView()
                .waitUntilDataLoading();

        listView.getTasksDataGrid()
                .getHeaderCellByIndex(0, PROCESS_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(GRID_COLUMN_SORTER_BY)
                .shouldBe(visible)
                .click();

        // then
        listView.getTasksDataGrid()
                .shouldHave(bodyCellTextsExactly(PROCESS_INSTANCE_ID_COLUMN_INDEX, sortedInstanceIds));
    }
}
