/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail;

import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Strings;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.component.BpmnViewerFragment.DIAGRAM_SVG_VIEWPORT_BY;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions with BPMN diagram in Process instance detail view")
public class ProcessInstanceDiagramActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Activity statistics action is hidden on process instance detail view")
    void givenExistingProcessInstance_whenOpenDetailView_thenActivityStatisticsActionNotAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getBpmnViewerFragment()
                .getViewActivityStatisticsButton()
                .shouldNotBe(EXIST)
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Documentation action is hidden on process instance detail view")
    void givenExistingProcessInstance_whenOpenDetailView_thenDocumentationActionNotAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        // then
        detailView.getBpmnViewerFragment()
                .getViewDocumentationButton()
                .shouldNotBe(EXIST)
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Zoom in action updates diagram zoom on process instance detail view")
    void givenExistingProcessInstance_whenClickZoomInAction_thenDiagramZoomUpdated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        BpmnViewerFragment bpmnViewerFragment = detailView.getBpmnViewerFragment();
        SelenideElement viewport = bpmnViewerFragment.getBpmnViewerContainer()
                .find(DIAGRAM_SVG_VIEWPORT_BY)
                .shouldBe(exist);
        String sourceTransform = Strings.nullToEmpty(viewport.attr("transform"));

        // when
        bpmnViewerFragment.getZoomInButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .click();

        // then
        viewport.shouldNotHave(attribute("transform", sourceTransform));
    }

    @Test
    @DisplayName("Zoom out action updates diagram zoom on process instance detail view")
    void givenExistingProcessInstance_whenClickZoomOutAction_thenDiagramZoomUpdated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        BpmnViewerFragment bpmnViewerFragment = detailView.getBpmnViewerFragment();
        SelenideElement viewport = bpmnViewerFragment.getBpmnViewerContainer()
                .find(DIAGRAM_SVG_VIEWPORT_BY)
                .shouldBe(exist);
        String sourceTransform = Strings.nullToEmpty(viewport.attr("transform"));

        // when
        bpmnViewerFragment.getZoomOutButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .click();

        // then
        viewport.shouldNotHave(attribute("transform", sourceTransform));
    }

    @Test
    @DisplayName("Reset zoom action resets diagram zoom on process instance detail view")
    void givenExistingProcessInstanceWithChangedZoom_whenClickResetZoomAction_thenDiagramZoomReset() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        MainView mainView = loginAsAdmin();

        ProcessInstanceDetailView detailView = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId);

        BpmnViewerFragment bpmnViewerFragment = detailView.getBpmnViewerFragment();
        SelenideElement viewport = bpmnViewerFragment.getBpmnViewerContainer()
                .find(DIAGRAM_SVG_VIEWPORT_BY)
                .shouldBe(exist);
        String sourceTransform = Strings.nullToEmpty(viewport.attr("transform"));

        bpmnViewerFragment.getZoomOutButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .click();

        viewport.shouldNotHave(attribute("transform", sourceTransform));

        // when
        bpmnViewerFragment.getResetZoomButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .click();

        // then
        viewport.shouldHave(attribute("transform", sourceTransform));
    }

    @Test
    @DisplayName("Called process instance overlay opens process instance detail view")
    void givenProcessInstanceWithCalledInstance_whenClickCalledProcessInstanceOverlay_thenCalledProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn")
                .startByKey("testSkipSubprocessMain");
        String parentInstanceId = dataManager.getStartedInstances("testSkipSubprocessMain").get(0);
        List<ProcessInstanceDto> subprocessInstances = camundaRestTestHelper.findRuntimeSubprocessInstances(camunda7, parentInstanceId);
        String subprocessInstanceId = subprocessInstances.get(0).getId();

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(parentInstanceId)
                .getBpmnViewerFragment();

        viewerFragment.getCalledProcessInstanceOverlay("subprocessTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + subprocessInstanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Called decision instance overlay opens decision instance detail view")
    void givenProcessInstanceWithDecisionInstance_whenClickDecisionInstanceOverlay_thenDecisionInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").get(0);
        String decisionInstanceId = dataManager.getDecisionInstances("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .getBpmnViewerFragment();

        viewerFragment.getDecisionInstanceOverlay("evaluateDecisionTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-instances/" + decisionInstanceId));

        $j(DecisionInstanceDetailView.class)
                .exists()
                .displayed();
    }
}
