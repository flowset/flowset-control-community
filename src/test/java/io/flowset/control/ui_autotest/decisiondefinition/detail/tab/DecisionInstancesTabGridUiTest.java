/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.detail.tab;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionInstancesTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.empty;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;
import static io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionInstancesTabFragment.EVALUATION_TIME_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Decision instances grid on Decision instances tab in decision detail view")
public class DecisionInstancesTabGridUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("The grid displays Id, Evaluation time, Process, Process instance, Activity columns")
    void givenExistingDecisionDefinition_whenOpenDecisionInstancesTab_thenGridDisplaysExpectedColumns() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        // then
        tab.getDecisionInstancesGrid()
                .shouldHave(headerCellTexts(List.of("Id", "Evaluation time", "Process", "Process instance",
                        "Activity")));
    }

    @Test
    @DisplayName("The grid displays evaluation time of a decision instance")
    void givenExistingDecisionInstance_whenOpenDecisionInstancesTab_thenEvaluationTimeShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        // then
        tab.getRowByInstanceId(decisionInstanceId)
                .getCellByIndex(EVALUATION_TIME_COLUMN_INDEX)
                .getCellContent()
                .shouldNotBe(empty);
    }
}
