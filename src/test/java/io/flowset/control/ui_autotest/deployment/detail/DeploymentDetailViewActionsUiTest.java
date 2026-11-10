/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.deployment.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentDetailView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentListView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.allBodyRowsHaveCellText;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.deployment.DeploymentDetailView.RESOURCE_NAME_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.deployment.DeploymentListView.DEPLOYMENT_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.deployment.DeploymentListView.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Deployment detail view")
public class DeploymentDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine engine;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;


    @Test
    @DisplayName("Close button closes the deployment detail view")
    void givenExistingDeployment_whenClickClose_thenDeploymentListViewOpened() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(engine, "test_support/vacationApproval.bpmn")
                .getId();

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.waitUntilDataLoading()
                .getRowByDeploymentId(deploymentId)
                .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        DeploymentDetailView detailView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();

        detailView.getCloseButton().click();

        // then
        detailView.shouldNotBe(EXIST);
    }

    @Test
    @DisplayName("Resources grid lists deployment resources")
    void givenExistingDeployment_whenOpenDeploymentDetailView_thenResourcesShown() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(engine, "test_support/vacationApproval.bpmn")
                .getId();

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.waitUntilDataLoading()
                .getRowByDeploymentId(deploymentId)
                .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        DeploymentDetailView detailView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();

        detailView.getResourcesDataGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(RESOURCE_NAME_COLUMN_INDEX, "vacationApproval.bpmn"));
        detailView.getEmptyResourceMessageContainer().shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Selecting BPMN resource shows its diagram")
    void givenExistingDeployment_whenSelectBpmnResource_thenDiagramShown() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(engine, "test_support/vacationApproval.bpmn")
                .getId();

        MainView mainView = loginAsAdmin();
        List<String> elementIds = List.of("startEvent", "approveVacationTask", "checkStateGateway", "saveDetailsTask",
                "approvedEndEvent", "notApprovedEndEvent");

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.waitUntilDataLoading()
                .getRowByDeploymentId(deploymentId)
                .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        DeploymentDetailView detailView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();

        detailView.selectResourceRow("vacationApproval.bpmn");

        // then
        detailView.getResourceTabSheet()
                .getTabById("viewTab")
                .shouldBe(SELECTED);

        BpmnViewerFragment viewerFragment = detailView.getBpmnViewerFragment();
        elementIds.forEach(elementId -> viewerFragment.getProcessDiagramElementById(elementId)
                .shouldBe(visible));
    }

    @Test
    @DisplayName("Selecting DMN resource shows its diagram")
    void givenExistingDecisionDeployment_whenSelectDmnResource_thenDiagramShown() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(engine, "test_support/dmn/testDmn.dmn")
                .getId();

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.waitUntilDataLoading()
                .getRowByDeploymentId(deploymentId)
                .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        DeploymentDetailView detailView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();

        detailView.selectResourceRow("testDmn.dmn");

        // then
        detailView.getResourceTabSheet()
                .getTabById("viewTab")
                .shouldBe(SELECTED);

        detailView.getDmnViewerFragment()
                .getDiagramElementById("decision_testDmn")
                .shouldBe(visible);
    }

    @Test
    @DisplayName("Running instances for BPMN resource grid navigation opens Process detail view")
    void givenDeploymentWithRunningInstance_whenProcessKeyClicked_thenProcessDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, engine)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String processDefinitionId = dataManager.getDeployedProcessVersions("vacation_approval").get(0);
        String deploymentId = camundaRestTestHelper.getProcessById(engine, processDefinitionId).getDeploymentId();

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.waitUntilDataLoading()
                .getRowByDeploymentId(deploymentId)
                .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        DeploymentDetailView detailView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();

        detailView.selectResourceRow("vacationApproval.bpmn");
        detailView.selectRunningInstancesTab()
                .openProcessFromRunningInstancesGrid("vacation_approval");

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processDefinitionId));

        ProcessDefinitionDetailView processDefinitionDetailView = $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();

        processDefinitionDetailView.getGeneralPanel()
                .getPropertiesPanel()
                .getKeyField()
                .shouldHave(value("vacation_approval"));
    }

    @Test
    @DisplayName("Copy deployment id action shows notification")
    void givenExistingDeployment_whenCopyDeploymentIdActionClicked_thenNotificationShown() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(engine, "test_support/vacationApproval.bpmn")
                .getId();

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.waitUntilDataLoading()
                .getRowByDeploymentId(deploymentId)
                .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        DeploymentDetailView detailView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();

        detailView.getCopyDeploymentIdBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("Copy name action shows notification")
    void givenExistingDeployment_whenCopyNameActionClicked_thenNotificationShown() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(engine, "test_support/vacationApproval.bpmn")
                .getId();

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.waitUntilDataLoading()
                .getRowByDeploymentId(deploymentId)
                .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        DeploymentDetailView detailView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();

        detailView.getCopyNameBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }
}
