/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail.tab;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionDiagramDialog;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.CalledDecisionsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.view.processdefinition.detail.CalledDecisionsTabFragment.*;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Decisions tab in process detail view")
public class DecisionsTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("List of available actions on Decisions tab")
    void givenExistingCalledDecision_whenOpenDetailView_thenAllDecisionActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .openDecisionsTab();

        // then
        decisionsTab.getRowByDecisionRef("decision_testDmn")
                .getCellByIndex(DECISION_REF_COLUMN_INDEX)
                .getCellContent()
                .find(KEY_BUTTON_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        decisionsTab.getRowByDecisionRef("decision_testDmn")
                .getCellByIndex(DECISION_REF_COLUMN_INDEX)
                .getCellContent()
                .find(PREVIEW_BUTTON_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("View action on Decisions tab opens decision detail view")
    void givenExistingCalledDecision_whenOpenDetailView_thenViewActionInDecisionRefOpensDetailView() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn");
        String decisionDefinitionId = dataManager.getDeployedDecisionVersions("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .openDecisionsTab();

        // then
        decisionsTab.getRowByDecisionRef("decision_testDmn")
                .getCellByIndex(DECISION_REF_COLUMN_INDEX)
                .getCellContent()
                .find(KEY_BUTTON_BY).click();

        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + decisionDefinitionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Double-click on grid row opens decision detail view")
    void givenExistingCalledDecision_whenOpenDetailViewAndDoubleClickRow_thenDecisionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn");
        String decisionDefinitionId = dataManager.getDeployedDecisionVersions("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .openDecisionsTab();

        // then
        decisionsTab.getRowByDecisionRef("decision_testDmn")
                .getCellByIndex(BINDING_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + decisionDefinitionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Preview action for decision on Decisions tab opens Preview dialog")
    void givenExistingCalledDecision_whenOpenDetailView_thenPreviewDialogOpened() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .openDecisionsTab();

        // then
        decisionsTab.getRowByDecisionRef("decision_testDmn")
                .getCellByIndex(DECISION_REF_COLUMN_INDEX)
                .getCellContent()
                .find(PREVIEW_BUTTON_BY).click();

        DecisionDefinitionDiagramDialog dialog = $j(DecisionDefinitionDiagramDialog.class)
                .exists()
                .displayed();

        dialog.getKeyField()
                .shouldBe(VISIBLE)
                .shouldHave(value("decision_testDmn"));
        dialog.getVersionField()
                .shouldBe(VISIBLE)
                .shouldHave(value("1"));
    }
}
