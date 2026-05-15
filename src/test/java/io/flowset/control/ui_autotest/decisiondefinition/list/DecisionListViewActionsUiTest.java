/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionDefinitionDetailView;
import io.flowset.control.test_support.ui.view.decisiondeployment.DecisionDeploymentView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.*;
import static io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Decision list view")
public class DecisionListViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Refresh action availability on decision definition list view")
    void givenLoggedInUser_whenOpenDecisionDefinitionList_thenRefreshActionEnabledRuleApplied() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");

        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();

        // then
        listView.getRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openDecisionGridContextActions()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action reloads data in the data grid")
    void givenNewlyDeployedDecisionDefinition_whenClickRefresh_thenGridReloaded() {
        // given
        MainView mainView = loginAsAdmin();
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();
        listView.waitUntilDataLoading();

        listView.getDecisionDefinitionsGrid()
                .shouldBe(emptyGrid);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");

        // when
        listView.getRefreshBtn().click();

        // then
        listView.getDecisionDefinitionsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(bodyRowWithCellTexts(Map.of(
                        KEY_COLUMN_INDEX, "decision_testDmn",
                        VERSION_COLUMN_INDEX, "1")));
    }

    @Test
    @DisplayName("Deploy action opens Decision deployment view")
    void givenLoggedInUser_whenOpenDecisionDefinitionListAndClickDeploy_thenDecisionDeploymentViewOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();
        listView.getDeployBtn().click();

        // then
        $j(DecisionDeploymentView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("View action in the Name column in row opens decision detail view")
    void givenExistingDecisionDefinition_whenClickViewActionInNameColumn_thenDecisionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");
        String decisionDefinitionId = dataManager.getDeployedDecisionVersions("decision_testDmn").get(0);
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();
        listView.getRowByDecisionKey("decision_testDmn")
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + decisionDefinitionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("View action in the Key column in row opens decision detail view")
    void givenExistingDecisionDefinition_whenClickViewActionInKeyColumn_thenDecisionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");
        String decisionDefinitionId = dataManager.getDeployedDecisionVersions("decision_testDmn").get(0);
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();
        listView.getRowByDecisionKey("decision_testDmn")
                .getCellByIndex(KEY_COLUMN_INDEX)
                .getCellContent()
                .find(KEY_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + decisionDefinitionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Double-clicking a row opens the decision detail view")
    void givenExistingDecisionDefinition_whenDoubleClickRow_thenDecisionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");
        String decisionDefinitionId = dataManager.getDeployedDecisionVersions("decision_testDmn").get(0);
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();
        listView.getRowByDecisionKey("decision_testDmn")
                .getDelegate()
                .doubleClick();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + decisionDefinitionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("List of available actions on decision definition list view")
    void givenExistingDecisionDefinition_whenOpenDecisionDefinitionList_thenAllActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");
        MainView mainView = loginAsAdmin();

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();

        // then
        listView.getRefreshBtn().shouldBe(VISIBLE);
        listView.getDeployBtn().shouldBe(VISIBLE);

        listView.openDecisionGridContextActions()
                .shouldHave(visibleItems("Refresh", "Deploy"));
    }
}
