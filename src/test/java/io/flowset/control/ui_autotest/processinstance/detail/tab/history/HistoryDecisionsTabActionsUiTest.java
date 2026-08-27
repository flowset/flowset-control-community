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
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceDetailDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryDecisionsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryDecisionsTabFragment.DECISION_INSTANCE_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryDecisionsTabFragment.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.visibleItemsCount;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the Decisions tab (History tab) in Process instance detail view")
public class HistoryDecisionsTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("No actions available for the Decisions data grid")
    void givenExistingHistoricDecision_whenOpenHistoryDecisionsTab_thenNoGridActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryDecisionsTabFragment decisionsTab = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryDecisionsTab();

        decisionsTab.selectRowByActivityId("evaluateDecisionTask");

        // then
        decisionsTab.openDecisionsGridContextMenu()
                .shouldHave(visibleItemsCount(0));
    }

    @Test
    @DisplayName("Decision instance ID link in data grid opens Historic decision instance detail dialog")
    void givenExistingHistoricDecision_whenDecisionInstanceIdLinkClicked_thenDecisionInstanceDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryDecisionsTabFragment decisionsTab = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryDecisionsTab();

        decisionsTab.getRowByActivityId("evaluateDecisionTask")
                .getCellByIndex(DECISION_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        $j(DecisionInstanceDetailDialog.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Double-clicking a decision row opens Historic decision instance detail dialog")
    void givenExistingHistoricDecision_whenRowDoubleClicked_thenDecisionInstanceDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").get(0);

        MainView mainView = loginAsAdmin();

        // when
        HistoryDecisionsTabFragment decisionsTab = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryDecisionsTab();

        decisionsTab.getRowByActivityId("evaluateDecisionTask")
                .getDelegate()
                .doubleClick();

        // then
        $j(DecisionInstanceDetailDialog.class).exists()
                .displayed();
    }
}
