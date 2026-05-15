/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisioninstance.detail;

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
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on decision instance detail view (route mode)")
public class DecisionInstanceDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Info button toggles the information panel visibility")
    void givenExistingDecisionInstance_whenInfoButtonClicked_thenInformationPanelToggled() {
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

        // then
        detailView.getInfoBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        detailView.getOpenDecisionDefinitionEditorBtn()
                .shouldNotBe(VISIBLE);

        detailView.getInfoBtn().click();
        detailView.getOpenDecisionDefinitionEditorBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        detailView.getInfoBtn().click();
        detailView.getOpenDecisionDefinitionEditorBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Open decision definition action navigates to Decision definition detail view")
    void givenExistingDecisionInstance_whenOpenDecisionDefinitionClickedOnDetailView_thenDecisionDefinitionDetailViewOpened() {
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
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);

        detailView.getInfoBtn().click();

        detailView.getOpenDecisionDefinitionEditorBtn().click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + decisionDefinitionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Open process instance action navigates to Process instance detail view")
    void givenExistingDecisionInstance_whenOpenProcessInstanceClickedOnDetailView_thenProcessInstanceDetailViewOpened() {
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
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);

        detailView.getInfoBtn().click();

        detailView.getOpenProcessInstanceEditorBtn().click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + processInstanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Open process action navigates to Process detail view")
    void givenExistingDecisionInstance_whenOpenProcessClickedOnDetailView_thenProcessDetailViewOpened() {
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
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);

        detailView.getInfoBtn().click();

        detailView.getOpenProcessDefinitionEditorBtn().click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processDefinitionId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Open process instance action is hidden for a standalone decision evaluation")
    void givenStandaloneDecisionInstance_whenOpenDetailView_thenOpenProcessInstanceButtonHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .evaluateDecisionByKey("decision_testDmn");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);

        detailView.getInfoBtn().click();

        // then
        detailView.getOpenProcessInstanceEditorBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Open process definition action is hidden for a standalone decision evaluation")
    void givenStandaloneDecisionInstance_whenOpenDetailView_thenOpenProcessDefinitionButtonHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .evaluateDecisionByKey("decision_testDmn");
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);

        detailView.getInfoBtn().click();

        // then
        detailView.getOpenProcessDefinitionEditorBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Close action closes the detail view")
    void givenExistingDecisionInstance_whenCloseClickedOnDetailView_thenDetailViewClosed() {
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

        detailView.getCloseButton().click();

        // then
        detailView.shouldNotBe(EXIST);
    }
}
