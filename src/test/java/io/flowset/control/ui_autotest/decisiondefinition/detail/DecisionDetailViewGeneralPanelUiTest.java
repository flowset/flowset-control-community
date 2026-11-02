/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionDefinitionDetailView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionGeneralPanelFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.Condition.exactValue;
import static com.codeborne.selenide.Condition.value;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("General panel of Decision detail view")
public class DecisionDetailViewGeneralPanelUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Version tag field shows version tag of the decision")
    void givenExistingDecisionDefinitionWithVersionTag_whenOpenDetailView_thenVersionTagShown() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn");
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionDetailView detailView = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn");

        // then
        detailView.getVersionTagField()
                .shouldBe(VISIBLE)
                .shouldHave(value("1.0"));
    }

    @Test
    @DisplayName("Deployment group shows deployment id, time and source of the decision deployment")
    void givenExistingDecisionDefinitionWithDeploymentSource_whenOpenDetailViewAndClickInfoToggle_thenDeploymentDataShown() {
        // given
        String deploymentId = camundaRestTestHelper.createDeploymentWithSource(camunda7,
                        "test_support/dmn/testDmn.dmn", "test-source")
                .getId();
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionDetailView detailView = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn");

        detailView.getGeneralPanel()
                .getInfoBtn().click();

        // then
        DecisionGeneralPanelFragment generalPanel = detailView.getGeneralPanel();
        generalPanel.getDeploymentIdField()
                .shouldHave(value(deploymentId));
        generalPanel.getDeploymentTimeField()
                .shouldBe(VISIBLE)
                .shouldNotHave(exactValue(""));
        generalPanel.getDeploymentSourceField()
                .shouldHave(value("test-source"));
    }
}
