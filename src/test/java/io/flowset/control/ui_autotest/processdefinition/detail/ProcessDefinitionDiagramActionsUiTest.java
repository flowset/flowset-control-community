/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail;

import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Strings;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.jmix.masquerade.component.Button;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.component.BpmnViewerFragment.DIAGRAM_SVG_VIEWPORT_BY;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@Slf4j
@WithRunningExternalEngine
@DisplayName("Actions on BPMN diagram viewer in process detail view")
public class ProcessDefinitionDiagramActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Called process overlay: opens process detail view for found called process")
    void givenProcessWithSubprocess_whenOpenDetailViewAndNavigate_thenCalledProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn");
        String subprocessId = sampleDataManager.getDeployedProcessVersions("testSkipSubprocess").get(0);

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("testSkipSubprocessMain")
                .getBpmnViewerFragment();

        viewerFragment.getCalledProcessOverlay("subprocessTask")
                .shouldBe(visible).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + subprocessId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Reset zoom action: updates diagram zoom")
    void givenExistingProcessDefinition_whenOpenDetailViewAndClickResetZoom_thenZoomReset() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        BpmnViewerFragment bpmnViewerFragment = detailView.getBpmnViewerFragment();

        SelenideElement viewport = bpmnViewerFragment.getBpmnViewerContainer()
                .find(DIAGRAM_SVG_VIEWPORT_BY)
                .shouldBe(exist);

        String sourceTransform = viewport.attr("transform");

        bpmnViewerFragment
                .getZoomOutButton()
                .click();

        sleep(500); // wait for zoom animation to complete

        // then
        detailView.getBpmnViewerFragment()
                .getResetZoomButton()
                .click();

        viewport.shouldHave(attribute("transform",
                Strings.nullToEmpty(sourceTransform)));
    }

    @Test
    @DisplayName("Zoom in action updates diagram zoom")
    void givenExistingProcessDefinition_whenOpenDetailViewAndClickZoomIn_thenZoomUpdated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        BpmnViewerFragment bpmnViewerFragment = detailView.getBpmnViewerFragment();

        SelenideElement viewport = bpmnViewerFragment.getBpmnViewerContainer()
                .find(DIAGRAM_SVG_VIEWPORT_BY)
                .shouldBe(exist);

        String sourceTransform = viewport.attr("transform");

        // then
        Button zoomInButton = detailView.getBpmnViewerFragment()
                .getZoomInButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        zoomInButton.click();

        viewport.shouldNotHave(attribute("transform",
                Strings.nullToEmpty(sourceTransform)));
    }

    @Test
    @DisplayName("Zoom out action updates diagram zoom")
    void givenExistingProcessDefinition_whenOpenDetailViewAndClickZoomOut_thenZoomUpdated() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        BpmnViewerFragment bpmnViewerFragment = detailView.getBpmnViewerFragment();

        SelenideElement viewport = bpmnViewerFragment.getBpmnViewerContainer()
                .find(DIAGRAM_SVG_VIEWPORT_BY)
                .shouldBe(exist);

        String sourceTransform = viewport.attr("transform");

        // then
        Button zoomOutButton = detailView.getBpmnViewerFragment()
                .getZoomOutButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        zoomOutButton.click();

        viewport.shouldNotHave(attribute("transform", Strings.nullToEmpty(sourceTransform)));
    }

    @Test
    @DisplayName("Decision overlay: opens decision detail view for found decision table")
    void givenProcessWithUsedDmn_whenOpenDetailViewAndNavigate_thenCalledProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String decisionId = sampleDataManager.getDeployedDecisionVersions("decision_testDmn").get(0);

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .getBpmnViewerFragment();

        viewerFragment.getDecisionOverlay("evaluateDecisionTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + decisionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Activity statistics action toggles overlay visibility")
    void givenProcessWithRunningInstances_whenClickActivityStatisticsAction_thenOverlaysVisibilityToggled() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 2);

        MainView mainView = loginAsAdmin();

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .getBpmnViewerFragment();

        SelenideElement overlay = viewerFragment.getActivityStatisticsOverlay("approveVacationTask");

        // then
        overlay.shouldBe(visible)
                .shouldHave(text("2"));

        viewerFragment.getViewActivityStatisticsButton().click();

        overlay.shouldNotBe(visible);

        viewerFragment.getViewActivityStatisticsButton().click();

        overlay.shouldBe(visible)
                .shouldHave(text("2"));
    }

    @Test
    @DisplayName("Documentation action availability")
    void givenExistingProcessDefinition_whenOpenDetailView_thenDocumentationActionNotAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");
        MainView mainView = loginAsAdmin();

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        detailView.getBpmnViewerFragment()
                .getViewDocumentationButton()
                .shouldNotBe(EXIST)
                .shouldNotBe(VISIBLE);
    }
}
