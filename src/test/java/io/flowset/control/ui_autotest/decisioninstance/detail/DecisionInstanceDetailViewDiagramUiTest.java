/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisioninstance.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.DmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.exactText;
import static io.flowset.control.test_support.ui.component.DmnViewerFragment.HIGHLIGHTED_CELL;

@WithRunningExternalEngine
@DisplayName("Diagram of Decision instance detail view")
public class DecisionInstanceDetailViewDiagramUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Matched rule is highlighted and its output cell shows the output value")
    void givenExistingDecisionInstance_whenOpenDetailView_thenMatchedRuleHighlighted() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        DmnViewerFragment dmnViewer = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId)
                .getDmnViewerFragment();

        // then
        dmnViewer.getDecisionTableCell("DecisionRule_1dt1dqx", "output_1937d")
                .shouldBe(HIGHLIGHTED_CELL)
                .shouldHave(exactText("\"ok\" = ok"));
        dmnViewer.getDecisionTableCell("DecisionRule_1dt1dqx", "input1")
                .shouldBe(HIGHLIGHTED_CELL);
        dmnViewer.getDecisionTableCell("DecisionRule_184tk79", "output_1937d")
                .shouldNotBe(HIGHLIGHTED_CELL)
                .shouldHave(exactText("\"not ok\""));
        dmnViewer.getDecisionTableCell("DecisionRule_184tk79", "input1")
                .shouldNotBe(HIGHLIGHTED_CELL);
    }
}
