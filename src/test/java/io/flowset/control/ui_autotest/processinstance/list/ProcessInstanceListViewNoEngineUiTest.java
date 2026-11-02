/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.list;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;

@DisplayName("Process instance list view with no BPM engine")
public class ProcessInstanceListViewNoEngineUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Opens with an empty grid and no exception dialog")
    void givenNoEngines_whenOpenProcessInstanceListView_thenEmptyGridShownWithoutErrors() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getProcessInstancesGrid().shouldBe(emptyGrid);
        $(JmixDialog.EXCEPTION_OVERLAY).shouldNot(exist);
    }

    @Test
    @DisplayName("The grid displays Id, Process, Business key, State, Start time, End time columns")
    void givenNoEngines_whenOpenProcessInstanceListView_thenGridColumnsShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        listView.getProcessInstancesGrid()
                .shouldHave(headerCellTexts(List.of("Id", "Process", "Business key", "State", "Start time",
                        "End time")));
    }
}
