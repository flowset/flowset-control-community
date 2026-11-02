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
import io.jmix.masquerade.component.Notification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

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

        bpmnViewerFragment
                .getZoomOutButton()
                .click();

        sleep(500); // wait for zoom animation to complete

        // then
        detailView.getBpmnViewerFragment()
                .getResetZoomButton()
                .click();

        viewport.shouldHave(attributeMatching("transform", "matrix\\(1 0 0 1 .*"));
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
    @DisplayName("Called process overlay with version binding opens the bound version")
    void givenProcessCallingByVersion_whenClickCalledProcessOverlay_thenBoundVersionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/binding/testCalledByVersion.bpmn")
                .deploy("test_support/binding/testCalledByVersion.bpmn")
                .deploy("test_support/binding/testCalledByVersion.bpmn")
                .deploy("test_support/testVersionBindings.bpmn");
        String boundVersionId = dataManager.getDeployedProcessVersions("testCalledByVersion").get(1);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .getBpmnViewerFragment()
                .getCalledProcessOverlay("callByVersionTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + boundVersionId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Called process overlay with versionTag binding opens the version with the tag")
    void givenProcessCallingByVersionTag_whenClickCalledProcessOverlay_thenTaggedVersionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/binding/testCalledByVersionTagV1.bpmn")
                .deploy("test_support/binding/testCalledByVersionTagV2.bpmn")
                .deploy("test_support/testVersionBindings.bpmn");
        String taggedVersionId = dataManager.getDeployedProcessVersions("testCalledByVersionTag").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .getBpmnViewerFragment()
                .getCalledProcessOverlay("callByVersionTagTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + taggedVersionId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Called process overlay with deployment binding opens the version from the same deployment")
    void givenProcessCallingByDeployment_whenClickCalledProcessOverlay_thenSameDeploymentVersionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy(List.of("test_support/binding/testDeploymentBindings.bpmn",
                        "test_support/binding/testCalledByDeployment.bpmn"))
                .deploy("test_support/binding/testCalledByDeployment.bpmn");
        String sameDeploymentVersionId = dataManager.getDeployedProcessVersions("testCalledByDeployment").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessListView()
                .openDetailViewByKey("testDeploymentBindings")
                .getBpmnViewerFragment()
                .getCalledProcessOverlay("callByDeploymentTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + sameDeploymentVersionId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Called process overlay with version binding to a missing version shows warning")
    void givenProcessCallingMissingVersion_whenClickCalledProcessOverlay_thenWarningShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/binding/testCalledByVersion.bpmn")
                .deploy("test_support/testVersionBindings.bpmn");
        String parentProcessId = dataManager.getDeployedProcessVersions("testVersionBindings").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .getBpmnViewerFragment()
                .getCalledProcessOverlay("callByVersionTask")
                .shouldBe(visible)
                .click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.WARNING))
                .shouldHave(text("Called process 'testCalledByVersion' not found"))
                .shouldHave(text("Binding: version"))
                .shouldHave(text("Version: 2"));

        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + parentProcessId));
    }

    @Test
    @DisplayName("Decision overlay with version binding opens the bound version")
    void givenProcessEvaluatingByVersion_whenClickDecisionOverlay_thenBoundVersionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/binding/testDecisionCalledByVersion.dmn")
                .deploy("test_support/binding/testDecisionCalledByVersion.dmn")
                .deploy("test_support/binding/testDecisionCalledByVersion.dmn")
                .deploy("test_support/binding/testDecisionCalledByVersion.dmn")
                .deploy("test_support/testVersionBindings.bpmn");
        String boundVersionId = dataManager.getDeployedDecisionVersions("decision_calledByVersion").get(2);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .getBpmnViewerFragment()
                .getDecisionOverlay("evaluateByVersionTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + boundVersionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Decision overlay with versionTag binding opens the version with the tag")
    void givenProcessEvaluatingByVersionTag_whenClickDecisionOverlay_thenTaggedVersionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/binding/testDecisionCalledByVersionTagV1.dmn")
                .deploy("test_support/binding/testDecisionCalledByVersionTagV2.dmn")
                .deploy("test_support/testVersionBindings.bpmn");
        String taggedVersionId = dataManager.getDeployedDecisionVersions("decision_calledByVersionTag").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .getBpmnViewerFragment()
                .getDecisionOverlay("evaluateByVersionTagTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + taggedVersionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Decision overlay with deployment binding opens the version from the same deployment")
    void givenProcessEvaluatingByDeployment_whenClickDecisionOverlay_thenSameDeploymentVersionDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy(List.of("test_support/binding/testDeploymentBindings.bpmn",
                        "test_support/binding/testDecisionCalledByDeployment.dmn"))
                .deploy("test_support/binding/testDecisionCalledByDeployment.dmn");
        String sameDeploymentVersionId = dataManager.getDeployedDecisionVersions("decision_calledByDeployment")
                .getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessListView()
                .openDetailViewByKey("testDeploymentBindings")
                .getBpmnViewerFragment()
                .getDecisionOverlay("evaluateByDeploymentTask")
                .shouldBe(visible)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/decision-definitions/" + sameDeploymentVersionId));

        $j(DecisionDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Decision overlay with version binding to a missing version shows warning")
    void givenProcessEvaluatingMissingVersion_whenClickDecisionOverlay_thenWarningShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/binding/testDecisionCalledByVersion.dmn")
                .deploy("test_support/testVersionBindings.bpmn");
        String parentProcessId = dataManager.getDeployedProcessVersions("testVersionBindings").getFirst();

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessListView()
                .openDetailViewByKey("testVersionBindings")
                .getBpmnViewerFragment()
                .getDecisionOverlay("evaluateByVersionTask")
                .shouldBe(visible)
                .click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.WARNING))
                .shouldHave(text("Decision table not found"))
                .shouldHave(text("Binding: version"))
                .shouldHave(text("Version: 3"));

        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + parentProcessId));
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
