/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.externaltask.RetryExternalTaskDialog;
import io.flowset.control.test_support.ui.view.incident.IncidentDataDetailDialog;
import io.flowset.control.test_support.ui.view.incident.IncidentDataDetailView;
import io.flowset.control.test_support.ui.view.job.RetryJobDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeIncidentsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.condition.ControlCondition.*;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.RuntimeIncidentsTabFragment.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the Incidents tab (Runtime tab) in Process instance detail view")
public class RuntimeIncidentsTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Retry action availability on Incidents tab in process instance detail view")
    void givenExistingJobIncident_whenOpenIncidentsTab_thenRetryActionAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        // then
        incidentsTab.getRetryButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        incidentsTab.selectRowByActivityId("throwsExceptionTask");

        incidentsTab.getRetryButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Retry action: job failed incident is removed from data grid after confirmation")
    void givenExistingJobIncident_whenOpenIncidentsTabAndRetry_thenIncidentRemovedFromGrid() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        incidentsTab.selectRowByActivityId("throwsExceptionTask");
        incidentsTab.getRetryButton().click();

        RetryJobDialog dialog = $j(RetryJobDialog.class).exists()
                .displayed();
        dialog.getRetriesField().setValue("5");
        dialog.getRetryBtn().click();

        // then
        incidentsTab.getRuntimeIncidentsGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Retry action: external task incident is removed from data grid after confirmation")
    void givenExistingExternalTaskIncident_whenOpenIncidentsTabAndRetry_thenIncidentRemovedFromGrid() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask");

        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        incidentsTab.selectRowByActivityId("failedExternalTask");
        incidentsTab.getRetryButton().click();

        RetryExternalTaskDialog dialog = $j(RetryExternalTaskDialog.class).exists()
                .displayed();
        dialog.getRetriesField().setValue("5");
        dialog.getRetryBtn().click();

        // then
        incidentsTab.getRuntimeIncidentsGrid()
                .shouldBe(emptyGrid);
    }

    @Test
    @DisplayName("Retry action: incident is not removed from data grid after cancellation")
    void givenExistingJobIncident_whenRetryCancelled_thenIncidentStillPresent() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        incidentsTab.selectRowByActivityId("throwsExceptionTask");
        incidentsTab.getRetryButton().click();

        RetryJobDialog dialog = $j(RetryJobDialog.class).exists()
                .displayed();
        dialog.getCancelBtn().click();

        // then
        incidentsTab.getRuntimeIncidentsGrid()
                .shouldHave(visibleBodyRowCount(1))
                .shouldHave(allBodyRowsHaveCellText(ACTIVITY_ID_COLUMN_INDEX, "throwsExceptionTask"));
    }

    @Test
    @DisplayName("Actions availability for incidents data grid")
    void givenExistingIncident_whenOpenIncidentsTab_thenActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        // then
        incidentsTab.getRetryButton().shouldBe(VISIBLE);

        incidentsTab.openIncidentsGridContextMenu()
                .shouldHave(visibleItems("Retry"));
    }

    @Test
    @DisplayName("Incident ID link in data grid opens Incident detail dialog")
    void givenExistingIncident_whenIncidentIdLinkClicked_thenIncidentDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        incidentsTab.getRowByActivityId("throwsExceptionTask")
                .getCellByIndex(INCIDENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        $j(IncidentDataDetailDialog.class).exists().displayed();

        IncidentDataDetailView detailView = $j(IncidentDataDetailView.class);
        detailView.getActivityIdField().shouldHave(value("throwsExceptionTask"));
        detailView.getProcessInstanceIdField().shouldHave(value(instanceId));
    }

    @Test
    @DisplayName("Double-clicking an incident row opens Incident detail dialog")
    void givenExistingIncident_whenRowDoubleClicked_thenIncidentDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        incidentsTab.getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        // then
        $j(IncidentDataDetailDialog.class).exists().displayed();

        IncidentDataDetailView detailView = $j(IncidentDataDetailView.class);
        detailView.getActivityIdField().shouldHave(value("throwsExceptionTask"));
        detailView.getProcessInstanceIdField().shouldHave(value(instanceId));
    }

    @Test
    @DisplayName("List of available grid actions on Incidents tab")
    void givenExistingIncident_whenOpenDetailView_thenAllIncidentsActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        RuntimeIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab();

        // then
        incidentsTab.getRetryButton().shouldBe(VISIBLE);

        incidentsTab.openIncidentsGridContextMenu()
                .shouldHave(visibleItems("Retry"));
    }
}
