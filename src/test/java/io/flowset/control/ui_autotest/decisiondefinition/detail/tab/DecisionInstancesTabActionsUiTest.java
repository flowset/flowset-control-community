/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.detail.tab;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.DecisionInstanceDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionInstancesTabFragment;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceDetailView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionInstancesTabFragment.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Decision instances tab in decision detail view")
public class DecisionInstancesTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("List of available actions on Decision instances tab")
    void givenExistingDecisionInstance_whenOpenDetailView_thenAllDecisionInstanceActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        // then
        tab.getRefreshButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        tab.openInstancesGridContextMenu()
                .shouldHave(visibleItems("Refresh"));
    }

    @Test
    @DisplayName("Refresh action reloads the decision instances grid")
    void givenNoEvaluatedDecisions_whenClickRefreshAfterEvaluation_thenGridReloaded() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");

        MainView mainView = loginAsAdmin();

        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        tab.getDecisionInstancesGrid()
                .shouldBe(emptyGrid);

        dataManager.evaluateDecisionByKey("decision_testDmn");

        // when
        tab.getRefreshButton().click();

        // then
        tab.getDecisionInstancesGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Decision instance ID link in row opens Decision instance detail view")
    void givenExistingDecisionInstance_whenIdLinkClicked_thenDecisionInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager camundaDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");

        String decisionInstanceId = camundaDataManager.getDecisionInstances("decision_testDmn").get(0);
        MainView mainView = loginAsAdmin();

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        tab.getRowByInstanceId(decisionInstanceId)
                .getCellByIndex(DECISION_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-instances/" + decisionInstanceId));

        $j(DecisionInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Double-clicking a decision instance row opens Decision instance detail view")
    void givenExistingDecisionInstance_whenRowDoubleClicked_thenDecisionInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager camundaDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");

        String decisionInstanceId = camundaDataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();


        tab.getRowByInstanceId(decisionInstanceId)
                .getCellByIndex(ACTIVITY_ID_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-instances/" + decisionInstanceId));

        $j(DecisionInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Process instance ID link in row opens Process instance detail view")
    void givenExistingDecisionInstance_whenProcessInstanceLinkClicked_thenProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");

        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);
        DecisionInstanceDto decisionInstance = camundaRestTestHelper.getDecisionInstanceById(camunda7, decisionInstanceId);
        String instanceId = decisionInstance.getProcessInstanceId();

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        tab.getRowByInstanceId(decisionInstanceId)
                .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(PROCESS_INSTANCE_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Process ID link in row opens Process detail view")
    void givenExistingDecisionInstance_whenProcessLinkClicked_thenProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");

        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);
        DecisionInstanceDto decisionInstance = camundaRestTestHelper.getDecisionInstanceById(camunda7, decisionInstanceId);
        String processId = decisionInstance.getProcessDefinitionId();

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        tab.getRowByInstanceId(decisionInstanceId)
                .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                .getCellContent()
                .find(PROCESS_ID_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Copy decision instance id action shows notification")
    void givenExistingDecisionInstance_whenCopyDecisionInstanceIdActionClicked_thenNotificationShown() {
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

        tab.getRowByInstanceId(decisionInstanceId)
                .getCellByIndex(DECISION_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        DecisionInstanceDetailView detailView = $j(DecisionInstanceDetailView.class).exists().displayed();

        detailView.getInfoBtn()
                .shouldBe(VISIBLE).click();

        detailView.getCopyDecisionInstanceIdBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }
}
