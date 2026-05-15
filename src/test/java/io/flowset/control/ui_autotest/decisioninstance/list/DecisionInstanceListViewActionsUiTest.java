/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisioninstance.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.DecisionInstanceDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionDefinitionDetailView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceDetailView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on decision instance list view")
public class DecisionInstanceListViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Refresh action availability on decision instance list view")
    void givenExistingDecisionInstance_whenOpenDecisionInstanceListView_thenRefreshActionAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        listView.getRefreshButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openInstancesGridContextMenu()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action: data in data grid is updated")
    void givenOpenedListView_whenClickRefresh_thenDataUpdated() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        listView.waitUntilDataLoading()
                .getDecisionInstancesDataGrid()
                .shouldBe(emptyGrid);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");

        listView.getRefreshButton().click();

        listView.getDecisionInstancesDataGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("List of available actions on decision instance list view")
    void givenExistingDecisionInstance_whenOpenDecisionInstanceListView_thenAllActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        listView.getRefreshButton().shouldBe(VISIBLE);

        listView.openInstancesGridContextMenu()
                .shouldHave(visibleItems("Refresh"));
    }

    @Test
    @DisplayName("Decision instance ID link in row opens Decision instance detail view")
    void givenExistingDecisionInstance_whenIdLinkClicked_thenDecisionInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        listView.getRowByDecisionInstanceId(decisionInstanceId)
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
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        listView.getRowByDecisionInstanceId(decisionInstanceId)
                .getCellByIndex(EVALUATION_TIME_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-instances/" + decisionInstanceId));

        $j(DecisionInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Link in Decision column row opens Decision detail view")
    void givenExistingDecisionInstance_whenDecisionLinkClicked_thenDecisionDefinitionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        DecisionInstanceDto decisionInstance = camundaRestTestHelper.getDecisionInstanceById(camunda7, decisionInstanceId);
        String decisionDefinitionId = decisionInstance.getDecisionDefinitionId();

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        listView.getRowByDecisionInstanceId(decisionInstanceId)
                .getCellByIndex(DECISION_DEFINITION_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + decisionDefinitionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Link in Process instance column opens Process instance detail view")
    void givenExistingDecisionInstance_whenProcessInstanceLinkClicked_thenProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);
        DecisionInstanceDto decisionInstance = camundaRestTestHelper.getDecisionInstanceById(camunda7, decisionInstanceId);
        String processInstanceId = decisionInstance.getProcessInstanceId();

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        listView.getRowByDecisionInstanceId(decisionInstanceId)
                .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(PROCESS_INSTANCE_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + processInstanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Link in Process column opens Process detail view")
    void givenExistingDecisionInstance_whenProcessLinkClicked_thenProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        DecisionInstanceDto decisionInstance = camundaRestTestHelper.getDecisionInstanceById(camunda7, decisionInstanceId);
        String processDefinitionId = decisionInstance.getProcessDefinitionId();

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        listView.getRowByDecisionInstanceId(decisionInstanceId)
                .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                .getCellContent()
                .find(PROCESS_ID_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processDefinitionId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }
}
