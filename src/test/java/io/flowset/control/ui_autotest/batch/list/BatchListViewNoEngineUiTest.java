/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.batch.list;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.AllBatchListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;

@DisplayName("Batch list view with no BPM engine")
public class BatchListViewNoEngineUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Opens with an empty Active tab grid and no exception dialog")
    void givenNoEngines_whenOpenBatchListView_thenEmptyActiveGridShownWithoutErrors() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        // then
        listView.getActiveBatchesDataGrid().shouldBe(emptyGrid);
        $(JmixDialog.EXCEPTION_OVERLAY).shouldNot(exist);
    }

    @Test
    @DisplayName("Active tab grid displays Id, Type, Start time, State, Failed jobs, Total jobs, Progress columns")
    void givenNoEngines_whenOpenBatchListView_thenActiveGridColumnsShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        // then
        listView.getActiveBatchesDataGrid()
                .shouldHave(headerCellTexts(List.of("Id", "Type", "Start time", "State", "Failed jobs", "Total jobs",
                        "Progress")));
    }

    @Test
    @DisplayName("Shows an empty Completed tab grid and no exception dialog")
    void givenNoEngines_whenSelectCompletedBatchesTab_thenEmptyCompletedGridShownWithoutErrors() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        // then
        listView.getCompletedBatchesDataGrid().shouldBe(emptyGrid);
        $(JmixDialog.EXCEPTION_OVERLAY).shouldNot(exist);
    }

    @Test
    @DisplayName("Completed tab grid displays Id, Type, Start date, End date, Total jobs columns")
    void givenNoEngines_whenSelectCompletedBatchesTab_thenCompletedGridColumnsShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        // then
        listView.getCompletedBatchesDataGrid()
                .shouldHave(headerCellTexts(List.of("Id", "Type", "Start date", "End date", "Total jobs")));
    }
}
