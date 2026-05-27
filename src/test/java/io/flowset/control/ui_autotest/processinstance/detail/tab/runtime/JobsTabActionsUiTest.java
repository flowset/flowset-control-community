/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.job.ActivateJobDialog;
import io.flowset.control.test_support.ui.view.job.JobDataDetailDialog;
import io.flowset.control.test_support.ui.view.job.RetryJobDialog;
import io.flowset.control.test_support.ui.view.job.SuspendJobDialog;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment.*;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions in the Jobs tab (Runtime tab) in Process instance detail view")
public class JobsTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("List of available actions on Jobs tab")
    void givenExistingJob_whenOpenDetailView_thenAllJobsActionsAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        jobsTab.getRetryButton().shouldBe(VISIBLE);
        jobsTab.getActivateButton().shouldBe(VISIBLE);
        jobsTab.getSuspendButton().shouldBe(VISIBLE);

        jobsTab.openJobsGridContextMenu()
                .shouldHave(visibleItems("Retry", "Activate", "Suspend"));
    }

    @Test
    @DisplayName("Retry action is enabled for selected failed job")
    void givenExistingFailedJob_whenOpenJobsTab_thenRetryActionAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        jobsTab.getRetryButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        jobsTab.selectRowByJobId(jobId);

        jobsTab.getRetryButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action is enabled for selected active job")
    void givenExistingActiveJob_whenOpenJobsTab_thenSuspendActionAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        jobsTab.getSuspendButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        jobsTab.selectRowByJobId(jobId);

        jobsTab.getSuspendButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Suspend action is not enabled for selected suspended job")
    void givenExistingSuspendedJob_whenOpenJobsTabAndSelectJob_thenSuspendActionNotEnabled() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);
        camundaRestTestHelper.suspendJobById(camunda7, jobId);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        jobsTab.getSuspendButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        jobsTab.selectRowByJobId(jobId);

        jobsTab.getSuspendButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Activate action is enabled for selected suspended job")
    void givenExistingSuspendedJob_whenOpenJobsTab_thenActivateActionAvailable() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);
        camundaRestTestHelper.suspendJobById(camunda7, jobId);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        jobsTab.getActivateButton()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);

        jobsTab.selectRowByJobId(jobId);

        jobsTab.getActivateButton()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Retry action: job retries are updated in data grid after confirmation")
    void givenExistingFailedJob_whenRetryConfirmed_thenJobRetriesUpdated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.selectRowByJobId(jobId);

        jobsTab.getRetryButton().click();

        RetryJobDialog dialog = $j(RetryJobDialog.class).exists()
                .displayed();

        dialog.getRetriesField().setValue("5");
        dialog.getRetryBtn().click();

        // then
        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(RETRIES_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("5"));
    }

    @Test
    @DisplayName("Retry action: job retries are not updated in data grid after cancellation")
    void givenExistingFailedJob_whenRetryCancelled_thenJobRetriesUnchanged() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.selectRowByJobId(jobId);

        jobsTab.getRetryButton().click();

        RetryJobDialog dialog = $j(RetryJobDialog.class)
                .exists()
                .displayed();

        dialog.getRetriesField().setValue("5");
        dialog.getCancelBtn().click();

        // then
        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(RETRIES_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("0"));
    }

    @Test
    @DisplayName("Suspend action: job state is updated in data grid after confirmation")
    void givenExistingActiveJob_whenSuspendConfirmed_thenJobSuspended() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.selectRowByJobId(jobId);

        jobsTab.getSuspendButton().click();

        $j(SuspendJobDialog.class).exists()
                .displayed()
                .getSuspendBtn()
                .click();

        // then
        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Suspended"));
    }

    @Test
    @DisplayName("Suspend action: job state is not changed in data grid after cancellation")
    void givenExistingActiveJob_whenSuspendCancelled_thenJobActive() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.selectRowByJobId(jobId);
        jobsTab.getSuspendButton().click();

        $j(SuspendJobDialog.class).exists()
                .displayed()
                .getCancelBtn()
                .click();

        // then
        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Active"));
    }

    @Test
    @DisplayName("Activate action: job is activated after confirmation")
    void givenExistingSuspendedJob_whenActivateConfirmed_thenJobActivated() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);
        camundaRestTestHelper.suspendJobById(camunda7, jobId);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.selectRowByJobId(jobId);

        jobsTab.getActivateButton().click();

        $j(ActivateJobDialog.class).exists()
                .displayed()
                .getActivateBtn()
                .click();

        // then
        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Active"));
    }

    @Test
    @DisplayName("Activate action: job is not activated after cancellation")
    void givenExistingSuspendedJob_whenActivateCancelled_thenJobSuspended() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);
        camundaRestTestHelper.suspendJobById(camunda7, jobId);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.selectRowByJobId(jobId);
        jobsTab.getActivateButton().click();

        $j(ActivateJobDialog.class).exists()
                .displayed()
                .getCancelBtn().click();

        // then
        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(STATE_COLUMN_INDEX)
                .getCellContent()
                .shouldHave(text("Suspended"));
    }

    @Test
    @DisplayName("Double-clicking a job row opens Job detail dialog")
    void givenExistingJob_whenRowDoubleClicked_thenJobDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(RETRIES_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        // then
        $j(JobDataDetailDialog.class).exists()
                .displayed()
                .getIdField()
                .shouldHave(value(jobId));

    }

    @Test
    @DisplayName("Link in Id column opens Job detail dialog")
    void givenExistingJob_whenIdLinkClicked_thenJobDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(JOB_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        $j(JobDataDetailDialog.class).exists()
                .displayed()
                .getIdField()
                .shouldHave(value(jobId));
    }
}
