/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.batch.detail;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.camunda7.dto.response.BatchDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.AllBatchListView;
import io.flowset.control.test_support.ui.view.batch.BatchDataDetailDialog;
import io.flowset.control.test_support.ui.view.batch.BatchStatisticsDataDetailDialog;
import io.flowset.control.test_support.ui.view.job.JobDataDetailDialog;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.*;
import static io.flowset.control.test_support.ui.view.batch.BatchStatisticsDataDetailDialog.JOB_ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.batch.BatchStatisticsDataDetailDialog.JOB_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JConditions.value;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Batch detail views")
public class BatchDetailViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Close action closes Batch data detail dialog")
    void testExistingCompletedBatch_whenCloseClickedOnBatchDataDetail_thenDialogClosed() {
        // given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batch = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.waitForBatchExecution(camunda7);
        String batchId = batch.getId();

        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        listView.getCompletedRowByBatchId(batchId)
                .getCellByIndex(COMPLETED_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        BatchDataDetailDialog dialog = $j(BatchDataDetailDialog.class)
                .exists()
                .displayed();

        dialog.getCloseBtn().click();

        // then
        dialog.shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Close action closes Batch statistics detail dialog")
    void testExistingActiveBatch_whenCloseClickedOnBatchStatisticsDetail_thenDialogClosed() {
        // given
        CamundaSampleDataManager camundaSampleDataManager =
                applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                        .deploy("test_support/testFailedBatch.bpmn")
                        .startByKey("testFailedBatch", 2);
        List<String> processInstanceIds = camundaSampleDataManager.getStartedInstances("testFailedBatch");
        BatchDto batch = camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);
        String batchId = batch.getId();

        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        listView.getActiveRowByBatchId(batchId)
                .getCellByIndex(ACTIVE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        BatchStatisticsDataDetailDialog dialog = $j(BatchStatisticsDataDetailDialog.class)
                .exists()
                .displayed();

        dialog.getCloseBtn().click();

        // then
        dialog.shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Job ID link in Batch statistics detail opens Job detail dialog")
    void testExistingActiveBatch_whenJobIdLinkClicked_thenJobDetailDialogOpened() {
        // given
        CamundaSampleDataManager camundaSampleDataManager =
                applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                        .deploy("test_support/testFailedBatch.bpmn")
                        .startByKey("testFailedBatch", StartProcessDto.builder()
                                        .variable("fail", new VariableValueDto("String", "testValue"))
                                        .build(),
                                2);
        List<String> processInstanceIds = camundaSampleDataManager.getStartedInstances("testFailedBatch");
        BatchDto batch = camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);
        String batchId = batch.getId();

        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        listView.getActiveRowByBatchId(batchId)
                .getCellByIndex(ACTIVE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        BatchStatisticsDataDetailDialog dialog = $j(BatchStatisticsDataDetailDialog.class)
                .exists()
                .displayed();

        DataGrid.Row jobRow = dialog.getJobsDataGrid()
                .getRowByIndex(0);
        jobRow.getDelegate()
                .shouldBe(visible);

        String jobId = jobRow.getCellByIndex(JOB_ID_COLUMN_INDEX)
                .getCellContent()
                .find(JOB_ID_BUTTON_BY)
                .getText();

        jobRow.getCellByIndex(JOB_ID_COLUMN_INDEX)
                .getCellContent()
                .find(JOB_ID_BUTTON_BY).click();

        // then
        $j(JobDataDetailDialog.class)
                .exists()
                .displayed()
                .getIdField()
                .shouldHave(value(jobId));
    }
}
