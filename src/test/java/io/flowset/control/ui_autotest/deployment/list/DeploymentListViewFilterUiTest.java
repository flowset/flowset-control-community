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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JConditions.dateValue;
import static io.jmix.masquerade.JConditions.timeValue;
import static io.jmix.masquerade.JConditions.value;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Filters on Deployment list view")
public class DeploymentListViewFilterUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Deployment after filter set to a past date keeps the deployment and is put to the URL")
    void givenExistingDeployment_whenSetDeploymentAfterInPast_thenDeploymentShown() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String pastDate = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        MainView mainView = loginAsAdmin();
        DeploymentListView listView = mainView.openDeploymentListView()
                .waitUntilDataLoading();

        // when
        listView.getDeploymentAfterField()
                .setDateValue(pastDate)
                .setTimeValue("00:00");

        // then
        webdriver().shouldHave(urlContaining("deployedAfter="));
        listView.getDeploymentsDataGrid()
                .shouldHave(visibleBodyRowCount(1));
        listView.getRowByName("vacationApproval.bpmn")
                .shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Deployment before filter set to a future date keeps the deployment and is put to the URL")
    void givenExistingDeployment_whenSetDeploymentBeforeInFuture_thenDeploymentShown() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String futureDate = LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        MainView mainView = loginAsAdmin();
        DeploymentListView listView = mainView.openDeploymentListView()
                .waitUntilDataLoading();

        // when
        listView.getDeploymentBeforeField()
                .setDateValue(futureDate)
                .setTimeValue("00:00");

        // then
        webdriver().shouldHave(urlContaining("deployedBefore="));
        listView.getDeploymentsDataGrid()
                .shouldHave(visibleBodyRowCount(1));
        listView.getRowByName("vacationApproval.bpmn")
                .shouldBe(VISIBLE);
    }

    @Test
    @DisplayName("Deployment after filter set to a future date hides the deployment and is put to the URL")
    void givenExistingDeployment_whenSetDeploymentAfterInFuture_thenDeploymentHidden() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String futureDate = LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        MainView mainView = loginAsAdmin();
        DeploymentListView listView = mainView.openDeploymentListView()
                .waitUntilDataLoading();

        // when
        listView.getDeploymentAfterField()
                .setDateValue(futureDate)
                .setTimeValue("00:00");

        // then
        listView.getDeploymentsDataGrid()
                .shouldBe(emptyGrid);
        webdriver().shouldHave(urlContaining("deployedAfter="));
    }

    @Test
    @DisplayName("Deployment before filter set to a past date hides the deployment and is put to the URL")
    void givenExistingDeployment_whenSetDeploymentBeforeInPast_thenDeploymentHidden() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String pastDate = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        MainView mainView = loginAsAdmin();
        DeploymentListView listView = mainView.openDeploymentListView()
                .waitUntilDataLoading();

        // when
        listView.getDeploymentBeforeField()
                .setDateValue(pastDate)
                .setTimeValue("00:00");

        // then
        listView.getDeploymentsDataGrid()
                .shouldBe(emptyGrid);
        webdriver().shouldHave(urlContaining("deployedBefore="));
    }

    @Test
    @DisplayName("deployedAfter query parameter in the future hides the deployment")
    void givenExistingDeployment_whenOpenWithDeployedAfterParamInUrl_thenFilterPreapplied() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        loginAsAdmin();

        // when
        open("/bpm/deployments?deployedAfter=" + now.plusDays(2));

        DeploymentListView listView = $j(DeploymentListView.class)
                .exists()
                .displayed()
                .waitUntilDataLoading();

        // then
        listView.getDeploymentsDataGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("deployedBefore query parameter in the past hides the deployment")
    void givenExistingDeployment_whenOpenWithDeployedBeforeParamInUrl_thenFilterPreapplied() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        loginAsAdmin();

        // when
        open("/bpm/deployments?deployedBefore=" + now.minusDays(2));

        DeploymentListView listView = $j(DeploymentListView.class)
                .exists()
                .displayed()
                .waitUntilDataLoading();

        // then
        listView.getDeploymentsDataGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Clear action resets filter field values")
    void givenAllFiltersSet_whenClickClear_thenFilterFieldsReset() {
        // given
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        loginAsAdmin();
        open("/bpm/deployments?name=contract&deployedAfter=" + now.plusDays(1) + "&deployedBefore=" + now.minusDays(1));

        DeploymentListView listView = $j(DeploymentListView.class)
                .exists()
                .displayed()
                .waitUntilDataLoading();

        // when
        listView.getClearBtn().click();

        // then
        listView.getNameField()
                .shouldHave(value(""));
        listView.getDeploymentAfterField()
                .shouldHave(dateValue(""))
                .shouldHave(timeValue(""));
        listView.getDeploymentBeforeField()
                .shouldHave(dateValue(""))
                .shouldHave(timeValue(""));
    }

    @Test
    @DisplayName("Clear action removes filter query parameters from the URL")
    void givenAllFiltersSet_whenClickClear_thenUrlFilterParamsRemoved() {
        // given
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        loginAsAdmin();
        open("/bpm/deployments?name=contract&deployedAfter=" + now.plusDays(1) + "&deployedBefore=" + now.minusDays(1));

        DeploymentListView listView = $j(DeploymentListView.class)
                .exists()
                .displayed()
                .waitUntilDataLoading();

        // when
        listView.getClearBtn().click();

        // then
        webdriver().shouldNotHave(urlContaining("name="));
        webdriver().shouldNotHave(urlContaining("deployedAfter="));
        webdriver().shouldNotHave(urlContaining("deployedBefore="));
    }

    @Test
    @DisplayName("Clear action reloads deployments without filters")
    void givenDeploymentHiddenByFilters_whenClickClear_thenDataReloaded() {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        loginAsAdmin();
        open("/bpm/deployments?name=contract&deployedAfter=" + now.plusDays(1) + "&deployedBefore=" + now.minusDays(1));

        DeploymentListView listView = $j(DeploymentListView.class)
                .exists()
                .displayed()
                .waitUntilDataLoading();

        // when
        listView.getClearBtn().click();

        // then
        listView.getDeploymentsDataGrid()
                .shouldHave(visibleBodyRowCount(1));
        listView.getRowByName("vacationApproval.bpmn")
                .shouldBe(VISIBLE);
    }
}
