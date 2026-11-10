/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.list;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;

@DisplayName("Decision list view with no BPM engine")
public class DecisionListViewNoEngineUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Opens with an empty grid and no exception dialog")
    void givenNoEngines_whenOpenDecisionListView_thenEmptyGridShownWithoutErrors() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();

        // then
        listView.getDecisionDefinitionsGrid().shouldBe(emptyGrid);
        $(JmixDialog.EXCEPTION_OVERLAY).shouldNot(exist);
    }

    @Test
    @DisplayName("The grid displays Name, Key, Version columns")
    void givenNoEngines_whenOpenDecisionListView_thenGridColumnsShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();

        // then
        listView.getDecisionDefinitionsGrid()
                .shouldHave(headerCellTexts(List.of("Name", "Key", "Version")));
    }
}
