/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisioninstance.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.empty;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.EVALUATION_TIME_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Decision instances grid on Decision instance list view")
public class DecisionInstanceListViewGridUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("The grid displays Id, Decision, Evaluation time, Process instance, Process, Activity columns")
    void givenLoggedInUser_whenOpenDecisionInstanceListView_thenGridDisplaysExpectedColumns() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView()
                .waitUntilDataLoading();

        // then
        listView.getDecisionInstancesDataGrid()
                .shouldHave(headerCellTexts(List.of("Id", "Decision", "Evaluation time", "Process instance",
                        "Process", "Activity")));
    }

    @Test
    @DisplayName("The grid displays evaluation time of a decision instance")
    void givenExistingDecisionInstance_whenOpenDecisionInstanceListView_thenEvaluationTimeShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        listView.getRowByDecisionInstanceId(decisionInstanceId)
                .getCellByIndex(EVALUATION_TIME_COLUMN_INDEX)
                .getCellContent()
                .shouldNotBe(empty);
    }
}
