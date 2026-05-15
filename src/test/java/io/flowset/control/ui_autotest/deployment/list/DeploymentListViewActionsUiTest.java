/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.deployment.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.deployment.BulkDeleteDeploymentDialog;
import io.flowset.control.test_support.ui.view.deployment.DeleteDeploymentDialog;
import io.flowset.control.test_support.ui.view.deployment.DeploymentDetailView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.deployment.DeploymentListView.DEPLOYMENT_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.deployment.DeploymentListView.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Deployment list view")
public class DeploymentListViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Refresh action availability on Deployment list view")
    void givenLoggedInUser_whenOpenDeploymentList_thenRefreshActionAvailable() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        listView.getRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openDeploymentsGridContextMenu()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action: data in data grid is updated")
    void givenExistingDeployment_whenClickRefresh_thenGridReloaded() {
        // given
        MainView mainView = loginAsAdmin();
        DeploymentListView listView = mainView.openDeploymentListView();

        listView.waitUntilDataLoading()
                .getDeploymentsDataGrid()
                .shouldBe(emptyGrid);

        // when
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");

        listView.getRefreshBtn().click();

        // then
        listView.getDeploymentsDataGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Deployment ID link in row opens Deployment detail view")
    void givenExistingDeployment_whenClickDeploymentIdLink_thenDeploymentDetailViewOpened() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn")
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
        webdriver().shouldHave(urlContaining("/bpm/deployments/" + deploymentId));

        $j(DeploymentDetailView.class)
                .exists();
    }

    @Test
    @DisplayName("Double-clicking a deployment row opens Deployment detail view")
    void givenExistingDeployment_whenDoubleClickRow_thenDeploymentDetailViewOpened() {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn")
                .getId();

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.waitUntilDataLoading()
                .getRowByDeploymentId(deploymentId)
                .getDelegate()
                .doubleClick();

        // then
        webdriver().shouldHave(urlContaining("/bpm/deployments/" + deploymentId));

        DeploymentDetailView detailView = $j(DeploymentDetailView.class)
                .exists()
                .displayed();
        detailView.getDeploymentIdField().shouldHave(value(deploymentId));
    }

    @Test
    @DisplayName("All actions visible in buttons panel and grid context menu")
    void givenExistingDeployment_whenOpenDeploymentList_thenAllActionsVisibleInButtonsPanelAndGridContextMenu() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        listView.getRefreshBtn().shouldBe(VISIBLE);
        listView.getBulkRemoveBtn().shouldBe(VISIBLE);

        listView.openDeploymentsGridContextMenu()
                .shouldHave(visibleItems("Refresh", "Remove"));
    }

    @Test
    @DisplayName("Remove action is enabled when at least one row is selected")
    void givenExistingDeployment_whenRowSelectedInGrid_thenRemoveActionEnabled() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        listView.getBulkRemoveBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        GridContextMenu gridContextMenu = listView.openDeploymentsGridContextMenu();
        gridContextMenu.find(text("Remove"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        gridContextMenu.close();

        listView.getDeploymentsDataGrid().clickSelectAll();

        listView.getBulkRemoveBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openDeploymentsGridContextMenu()
                .find(text("Remove"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Remove action: single deployment is removed from the data grid after confirmation")
    void givenSingleDeployment_whenRemoveConfirmed_thenDeploymentRemoved() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.getDeploymentsDataGrid().clickSelectAll();
        listView.getBulkRemoveBtn().click();

        $j(DeleteDeploymentDialog.class)
                .exists()
                .displayed()
                .getOkBtn().click();

        // then
        listView.waitUntilDataLoading()
                .getDeploymentsDataGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Remove action: single deployment is not removed from the data grid after cancellation")
    void givenSingleDeployment_whenRemoveCancelled_thenDeploymentNotRemoved() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.getDeploymentsDataGrid().clickSelectAll();
        listView.getBulkRemoveBtn().click();

        $j(DeleteDeploymentDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getDeploymentsDataGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Bulk Remove action: all selected deployments are removed from the data grid after confirmation")
    void givenMultipleDeployments_whenBulkRemoveConfirmed_thenAllSelectedDeploymentsRemoved() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        camundaRestTestHelper.createDeployment(camunda7, "test_support/testUserTaskWithAssignee.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.getDeploymentsDataGrid().clickSelectAll();
        listView.getBulkRemoveBtn().click();

        $j(BulkDeleteDeploymentDialog.class)
                .exists()
                .displayed()
                .getOkBtn().click();

        // then
        listView.waitUntilDataLoading()
                .getDeploymentsDataGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Bulk Remove action: selected deployments are not removed from the data grid after cancellation")
    void givenMultipleDeployments_whenBulkRemoveCancelled_thenAllSelectedDeploymentsNotRemoved() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        camundaRestTestHelper.createDeployment(camunda7, "test_support/testUserTaskWithAssignee.bpmn");

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();
        listView.getDeploymentsDataGrid().clickSelectAll();
        listView.getBulkRemoveBtn().click();

        $j(BulkDeleteDeploymentDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getDeploymentsDataGrid()
                .shouldHave(visibleBodyRowCount(2));
    }
}
