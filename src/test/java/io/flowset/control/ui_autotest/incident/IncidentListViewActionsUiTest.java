/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.incident;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.HandleFailureDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.notification.BatchNotificationContentFragment;
import io.flowset.control.test_support.ui.view.externaltask.RetryExternalTaskDialog;
import io.flowset.control.test_support.ui.view.incident.BulkRetryIncidentDialog;
import io.flowset.control.test_support.ui.view.incident.IncidentDataDetailView;
import io.flowset.control.test_support.ui.view.incident.IncidentListView;
import io.flowset.control.test_support.ui.view.job.RetryJobDialog;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.TagNames.NOTIFICATION_CARD;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on incident list view")
public class IncidentListViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Refresh action availability on incident list view")
    void givenExistingIncident_whenOpenIncidentListView_thenRefreshActionAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        listView.getRefreshButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openIncidentsGridContextMenu()
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
        IncidentListView listView = mainView.openIncidentListView();

        // then
        listView.waitUntilDataLoading()
                .getIncidentsGrid()
                .shouldBe(emptyGrid);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        listView.getRefreshButton().click();

        listView.getIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Bulk retry action availability on incident list view")
    void givenExistingIncident_whenOpenIncidentListView_thenBulkRetryActionAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        listView.getBulkRetryButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.openIncidentsGridContextMenu()
                .find(text("Retry"))
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        listView.getIncidentsGrid().clickSelectAll();

        listView.getBulkRetryButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openIncidentsGridContextMenu()
                .find(text("Retry"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Bulk retry action: incident is removed from data grid after confirmation")
    void givenExistingJobIncident_whenBulkRetryConfirmed_thenIncidentRemovedFromDataGrid() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();
        listView.waitUntilDataLoading()
                .getIncidentsGrid()
                .clickSelectAll();

        listView.getBulkRetryButton().click();

        BulkRetryIncidentDialog retryDialog = $j(BulkRetryIncidentDialog.class)
                .exists()
                .displayed();

        retryDialog.getRetryBtn().click();

        camundaRestTestHelper.waitForBatchExecution(camunda7);

        // then
        listView.getRefreshButton().click();
        listView.waitUntilDataLoading()
                .getIncidentsGrid()
                .shouldNotHave(emptyGrid);
    }

    @Test
    @DisplayName("Bulk retry action: incident is not removed from data grid after cancellation")
    void givenExistingJobIncident_whenBulkRetryCancelled_thenIncidentStillPresent() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();
        listView.getIncidentsGrid().clickSelectAll();

        listView.getBulkRetryButton().click();

        $j(BulkRetryIncidentDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Bulk retry action: notification is shown after confirmation")
    void givenExistingJobIncident_whenBulkRetryConfirmed_thenNotificationShown() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();
        listView.waitUntilDataLoading()
                .getIncidentsGrid()
                .clickSelectAll();

        listView.getBulkRetryButton().click();

        $j(BulkRetryIncidentDialog.class)
                .exists()
                .displayed()
                .getRetryBtn().click();

        // then
        BatchNotificationContentFragment notification = $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .exists()
                .displayed();

        notification.getTitleText()
                .shouldBe(VISIBLE)
                .shouldHave(text("Retries increment started"));
        notification.getBatchDescription()
                .shouldBe(VISIBLE)
                .shouldHave(text("Refresh data or view progress in Batch details"));
    }

    @Test
    @DisplayName("List of available actions on incident list view")
    void givenExistingIncident_whenOpenIncidentListView_thenAllIncidentActionsAvailable() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        listView.getRefreshButton().shouldBe(VISIBLE);
        listView.getBulkRetryButton().shouldBe(VISIBLE);

        listView.openIncidentsGridContextMenu()
                .shouldHave(visibleItems("Refresh", "Retry"));
    }

    @Test
    @DisplayName("Inline retry action is visible for root cause job-failure incident")
    void givenExistingJobFailedIncident_whenOpenIncidentListView_thenInlineRetryActionVisible() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        DataGrid.Row row = listView.getRowByActivityId("throwsExceptionTask");
        row.getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(INLINE_RETRY_BUTTON_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Inline retry action is visible for root cause external task incident")
    void givenExistingExternalTaskFailedIncident_whenOpenIncidentListView_thenInlineRetryActionVisible() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = camundaRestTestHelper.getExternalTaskIds(camunda7, List.of(instanceId)).get(0);
        camundaRestTestHelper.failExternalTask(camunda7, externalTaskId, HandleFailureDto.builder()
                .errorMessage("Test failure")
                .retries(0)
                .workerId("test-worker")
                .build());

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        DataGrid.Row row = listView.getRowByActivityId("failedExternalTask");
        row.getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(INLINE_RETRY_BUTTON_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Inline retry button is hidden for a custom incident")
    void givenExistingCustomIncident_whenOpenIncidentListView_thenInlineRetryButtonHidden() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testCustomIncidentFired.bpmn")
                .startByKey("testCustomIncidentFired");

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        listView.getRowByActivityId("fireCustomIncidentTask")
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(INLINE_RETRY_BUTTON_BY)
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Inline retry button is hidden for non-root cause incident")
    void givenExistingNonRootCauseIncident_whenOpenIncidentListView_thenInlineRetryButtonHidden() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .deploy("test_support/testPropagatedJobIncident.bpmn")
                .startByKey("testPropagatedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();
        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        listView.getRowByActivityId("failedSubprocessTask")
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(INLINE_RETRY_BUTTON_BY)
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Inline retry: job-failed incident is removed from data grid after confirmation")
    void givenExistingJobFailedIncident_whenInlineRetryConfirmed_thenIncidentResolved() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        DataGrid.Row row = listView.getRowByActivityId("throwsExceptionTask");
        row.getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(INLINE_RETRY_BUTTON_BY).click();

        $j(RetryJobDialog.class)
                .exists()
                .displayed()
                .getRetryBtn()
                .click();

        // then
        listView.waitUntilDataLoading()
                .getIncidentsGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Inline retry: job-failed incident is not removed from data grid after cancellation")
    void givenExistingJobFailedIncident_whenInlineRetryCancelled_thenIncidentStillPresent() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        DataGrid.Row row = listView.getRowByActivityId("throwsExceptionTask");
        row.getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(INLINE_RETRY_BUTTON_BY).click();

        $j(RetryJobDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Inline retry: external task incident is removed from data grid after confirmation")
    void givenExistingExternalTaskFailedIncident_whenInlineRetryConfirmed_thenIncidentResolved() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = camundaRestTestHelper.getExternalTaskIds(camunda7, List.of(instanceId)).get(0);
        camundaRestTestHelper.failExternalTask(camunda7, externalTaskId, HandleFailureDto.builder()
                .errorMessage("Test failure")
                .retries(0)
                .workerId("test-worker")
                .build());

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        DataGrid.Row row = listView.getRowByActivityId("failedExternalTask");
        row.getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(INLINE_RETRY_BUTTON_BY).click();

        $j(RetryExternalTaskDialog.class)
                .exists()
                .displayed()
                .getRetryBtn().click();

        // then
        listView.waitUntilDataLoading()
                .getIncidentsGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Inline retry: external task incident is not removed from data grid after cancellation")
    void givenExistingExternalTaskFailedIncident_whenInlineRetryCancelled_thenIncidentStillPresent() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String externalTaskId = camundaRestTestHelper.getExternalTaskIds(camunda7, List.of(instanceId)).get(0);
        camundaRestTestHelper.failExternalTask(camunda7, externalTaskId, HandleFailureDto.builder()
                .errorMessage("Test failure")
                .retries(0)
                .workerId("test-worker")
                .build());

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        DataGrid.Row row = listView.getRowByActivityId("failedExternalTask");
        row.getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(INLINE_RETRY_BUTTON_BY).click();

        $j(RetryExternalTaskDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        listView.getIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("Process instance ID link in incident grid opens Process instance detail view")
    void givenExistingIncident_whenProcessInstanceIdLinkClicked_thenProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        DataGrid.Row row = listView.getRowByActivityId("throwsExceptionTask");
        row.getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Double-clicking an incident row opens Incident detail view")
    void givenExistingIncident_whenRowDoubleClicked_thenIncidentDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String incidentId = camundaRestTestHelper.findRuntimeIncidentsByInstanceId(camunda7, instanceId).get(0).getId();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        listView.getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        // then

        webdriver().shouldHave(urlContaining("bpm/incidents/" + incidentId));

        $j(IncidentDataDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Incident ID link in incident grid opens Incident detail view")
    void givenExistingIncident_whenIncidentIdLinkClicked_thenIncidentDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String incidentId = camundaRestTestHelper.findRuntimeIncidentsByInstanceId(camunda7, instanceId).get(0).getId();

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        listView.getRowByActivityId("throwsExceptionTask")
                .getCellByIndex(INCIDENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("bpm/incidents/" + incidentId));

        $j(IncidentDataDetailView.class).exists()
                .displayed();
    }

    @Test
    @DisplayName("Process column link in incident grid opens Process definition detail view")
    void givenExistingIncident_whenProcessLinkClicked_thenProcessDefinitionDetailViewOpened() {
        // given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String processId = camundaSampleDataManager.getDeployedProcessVersions("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentListView listView = mainView.openIncidentListView();

        listView.getRowByActivityId("throwsExceptionTask")
                .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }
}
