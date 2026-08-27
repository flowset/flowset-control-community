/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.incident.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.externaltask.RetryExternalTaskDialog;
import io.flowset.control.test_support.ui.view.incident.IncidentDataDetailView;
import io.flowset.control.test_support.ui.view.job.RetryJobDialog;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.text;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on incident detail view (route mode)")
public class IncidentDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Retry action is visible for a root cause job-failure incident")
    void givenExistingJobFailedIncident_whenOpenIncidentDetailView_thenRetryActionVisible() {
        // given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        String incidentId = camundaSampleDataManager.getIncidentsByKey("testFailedJobIncident").get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        // then
        detailView.getRetryBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Retry action is hidden for a custom incident")
    void givenExistingCustomIncident_whenOpenIncidentDetailView_thenRetryActionHidden() {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testCustomIncidentFired.bpmn")
                .startByKey("testCustomIncidentFired")
                .getIncidentsByKey("testCustomIncidentFired")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        // then
        detailView.getRetryBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Retry action is hidden for a non-root cause job-failure incident")
    void givenExistingNonRootCauseJobIncident_whenOpenIncidentDetailView_thenRetryActionHidden() {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .deploy("test_support/testPropagatedJobIncident.bpmn")
                .startByKey("testPropagatedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testPropagatedJobIncident")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        // then
        detailView.getRetryBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Retry action: detail view closes after confirmation")
    void givenExistingJobFailedIncident_whenRetryConfirmedOnDetailView_thenDetailViewClosed() {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        detailView.getRetryBtn().click();

        $j(RetryJobDialog.class)
                .exists()
                .displayed()
                .getRetryBtn()
                .click();

        // then
        detailView.shouldNotBe(EXIST);
    }

    @Test
    @DisplayName("Retry action: detail view stays open after cancellation")
    void givenExistingJobFailedIncident_whenRetryCancelledOnDetailView_thenDetailViewStillOpen() {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        detailView.getRetryBtn().click();

        $j(RetryJobDialog.class)
                .exists()
                .displayed()
                .getCancelBtn().click();

        // then
        detailView.exists()
                .displayed();
    }

    @Test
    @DisplayName("Retry action opens Retry external task dialog for an external task incident")
    void givenExistingExternalTaskFailedIncident_whenRetryClickedOnDetailView_thenRetryExternalTaskDialogOpened() {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask")
                .getIncidentsByKey("testFailedExternalTask")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        detailView.getRetryBtn().click();

        // then
        $j(RetryExternalTaskDialog.class)
                .exists()
                .displayed();
    }

    @Test
    @DisplayName("Copy id action shows notification")
    void givenExistingIncident_whenCopyIdActionClicked_thenNotificationShown() {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        detailView.getCopyIdBtn().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTheme(Notification.Theme.SUCCESS))
                .shouldHave(text("Value copied!"));
    }

    @Test
    @DisplayName("Close action closes to incident detail view")
    void givenExistingIncident_whenCloseClickedOnDetailView_thenIncidentDetailViewClosed() {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        MainView mainView = loginAsAdmin();

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        detailView.getCloseBtn().click();

        // then
        detailView.shouldNotBe(EXIST);
    }
}
