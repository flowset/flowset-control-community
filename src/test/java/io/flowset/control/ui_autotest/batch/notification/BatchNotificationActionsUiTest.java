/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.batch.notification;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.AllBatchListViewDialog;
import io.flowset.control.test_support.ui.view.batch.BatchDataDetailDialog;
import io.flowset.control.test_support.ui.view.batch.notification.BatchNotificationContentFragment;
import io.flowset.control.test_support.ui.view.incident.BulkRetryIncidentDialog;
import io.flowset.control.test_support.ui.view.incident.IncidentListView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView;
import io.flowset.control.test_support.ui.view.processinstance.action.BulkTerminateProcessInstanceDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.codeborne.selenide.Condition.empty;
import static io.flowset.control.test_support.ui.TagNames.NOTIFICATION_CARD;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on notification with batch details")
public class BatchNotificationActionsUiTest extends AbstractCamunda7UiTest {
    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Batch detail button opens Batch data detail dialog if batch id is set")
    void givenSingleBatchNotification_whenBatchDetailClicked_thenBatchDataDetailDialogOpened() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAsAdmin();

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();
        listView.getProcessInstancesGrid().clickSelectAll();
        listView.getBulkTerminateBtn().click();

        $j(BulkTerminateProcessInstanceDialog.class)
                .exists()
                .displayed()
                .getOkBtn().click();

        BatchNotificationContentFragment notification = $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .exists()
                .displayed();

        notification.getOpenBatchBtn().click();

        // then
        BatchDataDetailDialog dialog = $j(BatchDataDetailDialog.class)
                .exists()
                .displayed();
        dialog.getIdField()
                .getDelegate()
                .shouldNotBe(empty);
    }

    @Test
    @DisplayName("Batch detail button opens All batch list view in dialog if batch id is not set")
    void givenMultiBatchNotification_whenBatchDetailClicked_thenAllBatchListViewOpenedInDialog() {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask");

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

        BatchNotificationContentFragment notification = $j(BatchNotificationContentFragment.class, NOTIFICATION_CARD)
                .exists()
                .displayed();

        notification.getOpenBatchBtn().click();

        // then
        $j(AllBatchListViewDialog.class)
                .exists()
                .displayed();
    }
}
