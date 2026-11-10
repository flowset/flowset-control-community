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
import io.flowset.control.test_support.ui.component.DmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.Condition.visible;

@WithRunningExternalEngine
@DisplayName("DMN diagram in Decision definition detail view")
public class DecisionDefinitionDetailViewDiagramUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Decision table of the decision is shown")
    void givenExistingDecisionDefinition_whenOpenDetailView_thenDecisionTableShown() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn");
        MainView mainView = loginAsAdmin();

        // when
        DmnViewerFragment dmnViewer = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .getDmnViewerFragment();

        // then
        dmnViewer.getDiagramElementById("DecisionRule_1dt1dqx")
                .shouldBe(visible);
        dmnViewer.getDiagramElementById("DecisionRule_184tk79")
                .shouldBe(visible);
    }
}
