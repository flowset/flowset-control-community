/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.usertask.list;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.usertask.AllTasksListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;

@DisplayName("User task list view with no BPM engine")
public class UserTaskListViewNoEngineUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Opens with an empty grid and no exception dialog")
    void givenNoEngines_whenOpenUserTaskListView_thenEmptyGridShownWithoutErrors() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        listView.getTasksDataGrid().shouldBe(emptyGrid);
        $(JmixDialog.EXCEPTION_OVERLAY).shouldNot(exist);
    }

    @Test
    @DisplayName("The grid displays Id, Key, Name, Process instance, Process, Creation time, Assignee columns")
    void givenNoEngines_whenOpenUserTaskListView_thenGridColumnsShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllTasksListView listView = mainView.openUserTaskListView();

        // then
        listView.getTasksDataGrid()
                .shouldHave(headerCellTexts(List.of("Id", "Key", "Name", "Process instance", "Process",
                        "Creation time", "Assignee")));
    }
}
