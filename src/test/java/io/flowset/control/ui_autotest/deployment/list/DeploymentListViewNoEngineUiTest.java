/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.deployment.list;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.headerCellTexts;
import static io.jmix.masquerade.JConditions.VISIBLE;

@DisplayName("Deployment list view with no BPM engine")
public class DeploymentListViewNoEngineUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Opens with an empty grid and no exception dialog")
    void givenNoEngines_whenOpenDeploymentListView_thenEmptyGridShownWithoutErrors() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        listView.getDeploymentsDataGrid().shouldBe(emptyGrid);
        $(JmixDialog.EXCEPTION_OVERLAY).shouldNot(exist);
    }

    @Test
    @DisplayName("The grid displays Deployment id, Name, Deployment time, Source, Tenant id columns")
    void givenNoEngines_whenOpenDeploymentListView_thenGridColumnsShown() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        listView.getDeploymentsDataGrid()
                .shouldHave(headerCellTexts(List.of("Deployment id", "Name", "Deployment time", "Source",
                        "Tenant id")));
    }

    @Test
    @DisplayName("Deployment after and Deployment before filter fields are shown")
    void givenNoEngines_whenOpenDeploymentListView_thenDeploymentTimeFilterFieldsVisible() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        listView.getDeploymentAfterField().shouldBe(VISIBLE);
        listView.getDeploymentBeforeField().shouldBe(VISIBLE);
    }
}
