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
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.exactValue;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JConditions.label;

@WithRunningExternalEngine
@DisplayName("General panel of Decision instance detail view")
public class DecisionInstanceDetailViewGeneralPanelUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Evaluation time field shows evaluation time of the decision instance")
    void givenExistingDecisionInstance_whenInfoButtonClicked_thenEvaluationTimeShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);

        detailView.getInfoBtn().click();

        // then
        detailView.getEvaluationTimeField()
                .shouldBe(VISIBLE)
                .shouldHave(label("Evaluation time"))
                .shouldNotHave(exactValue(""));
    }
}
