/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.job.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.job.JobDataDetailDialog;
import io.flowset.control.test_support.ui.view.job.RetryJobDialog;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Selenide.switchTo;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.numberOfWindows;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment.JOB_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Job detail view (dialog mode)")
public class JobDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Navigation button in the Process field opens detail view in new tab")
    void givenExistingJob_whenViewProcessClicked_thenProcessDefinitionDetailOpenedInNewTab() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String jobId = dataManager.getJobsByKey("testFailedJobIncident").get(0);
        String processId = dataManager.getDeployedProcessVersions("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        jobsTab.getRowByJobId(jobId)
                .getCellByIndex(JOB_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .click();

        JobDataDetailDialog detailDialog = $j(JobDataDetailDialog.class)
                .exists()
                .displayed();
        detailDialog.getViewProcessBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Close button closes Job detail dialog")
    void givenExistingJob_whenCloseClicked_thenJobDetailDialogClosed() {
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
                .find(ID_BUTTON_BY)
                .click();

        JobDataDetailDialog detailDialog = $j(JobDataDetailDialog.class)
                .exists()
                .displayed();
        detailDialog.getCloseBtn().click();

        // then
        detailDialog.shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Navigation button in the Process instance field opens detail view in new tab")
    void givenExistingJob_whenViewProcessInstanceClicked_thenProcessInstanceDetailOpenedInNewTab() {
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
                .find(ID_BUTTON_BY)
                .click();

        JobDataDetailDialog detailDialog = $j(JobDataDetailDialog.class)
                .exists()
                .displayed();
        detailDialog.getViewProcessInstanceBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Retry action closes Job detail dialog after confirmation")
    void givenExistingFailedJob_whenRetryConfirmed_thenJobDetailDialogClosed() {
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
                .find(ID_BUTTON_BY)
                .click();

        JobDataDetailDialog detailDialog = $j(JobDataDetailDialog.class)
                .exists()
                .displayed();
        detailDialog.getRetryBtn().click();

        RetryJobDialog retryDialog = $j(RetryJobDialog.class)
                .exists()
                .displayed();
        retryDialog.getRetriesField().setValue("5");
        retryDialog.getRetryBtn().click();

        // then
        retryDialog.shouldNotBe(EXIST);
        detailDialog.shouldNotBe(EXIST);
    }
}
