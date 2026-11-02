/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisioninstance.list;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;

@DisplayName("Decision instance list view with no BPM engine")
public class DecisionInstanceListViewNoEngineUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Opens with an empty grid and no exception dialog")
    void givenNoEngines_whenOpenDecisionInstanceListView_thenEmptyGridShownWithoutErrors() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        listView.getDecisionInstancesDataGrid().shouldBe(emptyGrid);
        $(JmixDialog.EXCEPTION_OVERLAY).shouldNot(exist);
    }

    @Test
    @DisplayName("The grid displays Id, Decision, Evaluation time, Process instance, Process, Activity columns")
    void givenNoEngines_whenOpenDecisionInstanceListView_thenGridColumnsShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        listView.getDecisionInstancesDataGrid()
                .shouldHave(headerCellTexts(List.of("Id", "Decision", "Evaluation time", "Process instance",
                        "Process", "Activity")));
    }
}
