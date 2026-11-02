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
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentListView;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.deployment.DeploymentListView.*;

@WithRunningExternalEngine
@DisplayName("Deployments grid on Deployment list view")
public class DeploymentListViewGridUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("The grid displays Deployment id, Name, Deployment time, Source, Tenant id columns")
    void givenLoggedInUser_whenOpenDeploymentList_thenGridDisplaysExpectedColumns() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView()
                .waitUntilDataLoading();

        // then
        listView.getDeploymentsDataGrid()
                .shouldHave(headerCellTexts(List.of("Deployment id", "Name", "Deployment time", "Source",
                        "Tenant id")));
    }

    @Test
    @DisplayName("The grid displays deployment id, name, deployment time and source of a deployment")
    void givenExistingDeploymentWithSource_whenOpenDeploymentList_thenDeploymentDataDisplayedInColumns() {
        // given
        String deploymentId = camundaRestTestHelper.createDeploymentWithSource(camunda7,
                        "test_support/vacationApproval.bpmn", "test-source")
                .getId();

        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        listView.getDeploymentsDataGrid()
                .shouldHave(visibleBodyRowCount(1));

        DataGrid.Row row = listView.getRowByName("vacationApproval.bpmn");
        row.getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .shouldHave(text(deploymentId));
        row.getCellByIndex(DEPLOYMENT_TIME_COLUMN_INDEX)
                .getCellContent()
                .shouldNotBe(empty);
        row.getCellByIndex(SOURCE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("test-source"));
    }
}
