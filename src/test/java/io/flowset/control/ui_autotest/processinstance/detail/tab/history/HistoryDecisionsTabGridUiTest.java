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
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryDecisionsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.empty;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryDecisionsTabFragment.EVALUATION_TIME_COLUMN_INDEX;

@WithRunningExternalEngine
@DisplayName("Decisions grid in the Decisions tab (History tab) in Process instance detail view")
public class HistoryDecisionsTabGridUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("The grid displays Id, Activity, Decision, Evaluation time columns")
    void givenExistingHistoricDecision_whenOpenHistoryDecisionsTab_thenGridDisplaysExpectedColumns() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        HistoryDecisionsTabFragment decisionsTab = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryDecisionsTab();

        // then
        decisionsTab.getHistoryDecisionsGrid()
                .shouldHave(headerCellTexts(List.of("Id", "Activity", "Decision", "Evaluation time")));
    }

    @Test
    @DisplayName("The grid displays evaluation time of a decision instance")
    void givenExistingHistoricDecision_whenOpenHistoryDecisionsTab_thenEvaluationTimeShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        HistoryDecisionsTabFragment decisionsTab = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryDecisionsTab();

        // then
        decisionsTab.getRowByActivityId("evaluateDecisionTask")
                .getCellByIndex(EVALUATION_TIME_COLUMN_INDEX)
                .getCellContent()
                .shouldNotBe(empty);
    }
}
