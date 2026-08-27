/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.incident.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.externaltask.ExternalTaskDataDetailDialog;
import io.flowset.control.test_support.ui.view.incident.IncidentDataDetailDialog;
import io.flowset.control.test_support.ui.view.incident.IncidentDataDetailView;
import io.flowset.control.test_support.ui.view.job.JobDataDetailDialog;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.switchTo;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.numberOfWindows;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Navigation actions on incident detail view (dialog mode)")
public class IncidentDetailViewDialogNavigationActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Navigation button in the Process field opens Process definition detail view in a new browser tab")
    void givenExistingIncident_whenViewProcessLinkClicked_thenProcessDefinitionDetailViewOpened() {
        // given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String processId = camundaSampleDataManager.getDeployedProcessVersions("testFailedJobIncident").get(0);
        String instanceId = camundaSampleDataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        $j(IncidentDataDetailView.class).getViewProcessBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/process-definitions/" + processId));

        $j(ProcessDefinitionDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Process instance field opens Process instance detail view in a new browser tab")
    void givenExistingIncident_whenViewProcessInstanceLinkClicked_thenProcessInstanceDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        $j(IncidentDataDetailView.class).getViewProcessInstanceBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/process-instances/" + instanceId));

        $j(ProcessInstanceDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Cause incident field is hidden for a root-cause incident")
    void givenExistingRootCauseIncident_whenOpenIncidentDetailDialog_thenViewCauseIncidentBtnHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        // then
        $j(IncidentDataDetailView.class).getViewCauseIncidentBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Navigation button in the Root cause incident field is hidden for a root-cause incident")
    void givenExistingRootCauseIncident_whenOpenIncidentDetailDialog_thenViewRootCauseIncidentBtnHidden() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        // then
        $j(IncidentDataDetailView.class).getViewRootCauseIncidentBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Navigation button in the Root cause incident field opens the root-cause incident detail view in a new browser tab")
    void givenExistingNonRootCauseIncident_whenViewRootCauseIncidentLinkClicked_thenRootCauseIncidentDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .deploy("test_support/testPropagatedJobIncident.bpmn")
                .startByKey("testPropagatedJobIncident")
                .waitJobsExecution();
        String parentInstanceId = dataManager.getStartedInstances("testPropagatedJobIncident").get(0);
        String rootCauseIncidentId = camundaRestTestHelper.getIncidentIdsByProcessKey(camunda7, "testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(parentInstanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("failedSubprocessTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        $j(IncidentDataDetailView.class).getViewRootCauseIncidentBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/incidents/" + rootCauseIncidentId));

        $j(IncidentDataDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Cause incident field opens the cause incident detail view in a new browser tab")
    void givenExistingNonRootCauseIncident_whenViewCauseIncidentLinkClicked_thenCauseIncidentDetailViewOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .deploy("test_support/testPropagatedJobIncident.bpmn")
                .startByKey("testPropagatedJobIncident")
                .waitJobsExecution();
        String parentInstanceId = dataManager.getStartedInstances("testPropagatedJobIncident").get(0);
        String causeIncidentId = camundaRestTestHelper.getIncidentIdsByProcessKey(camunda7, "testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(parentInstanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("failedSubprocessTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        $j(IncidentDataDetailView.class).getViewCauseIncidentBtn().click();

        // then
        webdriver().shouldHave(numberOfWindows(2));
        switchTo().window(1);

        webdriver().shouldHave(urlContaining("/bpm/incidents/" + causeIncidentId));

        $j(IncidentDataDetailView.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Navigation button in the Configuration field for a job-failure incident opens Job detail dialog")
    void givenExistingJobFailedIncident_whenConfigurationLinkClicked_thenJobDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);
        String failedJobId = camundaRestTestHelper.getJobIdsByProcessKey(camunda7, "testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        $j(IncidentDataDetailView.class).getConfigurationBtn().click();

        // then
        JobDataDetailDialog jobDialog = $j(JobDataDetailDialog.class)
                .exists()
                .displayed();
        jobDialog.getIdField().shouldHave(value(failedJobId));
    }

    @Test
    @DisplayName("Copy id action shows notification")
    void givenExistingIncident_whenCopyIdActionClickedOnDialog_thenNotificationShown() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("throwsExceptionTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        $j(IncidentDataDetailView.class).getCopyIdBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("Navigation button in the Configuration field for an external-task-failure incident opens External task detail dialog")
    void givenExistingExternalTaskFailedIncident_whenConfigurationLinkClicked_thenExternalTaskDetailDialogOpened() {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);
        String failedExternalTaskId = camundaRestTestHelper.getExternalTaskIds(camunda7, java.util.List.of(instanceId)).get(0);

        MainView mainView = loginAsAdmin();

        // when
        mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeIncidentsTab()
                .getRowByActivityId("failedExternalTask")
                .getDelegate()
                .doubleClick();

        $j(IncidentDataDetailDialog.class)
                .exists()
                .displayed();

        $j(IncidentDataDetailView.class).getConfigurationBtn().click();

        // then
        ExternalTaskDataDetailDialog externalTaskDialog = $j(ExternalTaskDataDetailDialog.class)
                .exists()
                .displayed();
        externalTaskDialog.getExternalTaskIdField().shouldHave(value(failedExternalTaskId));
    }
}
