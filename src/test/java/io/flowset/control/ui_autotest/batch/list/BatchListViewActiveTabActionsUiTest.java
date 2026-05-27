/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.batch.list;

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
import io.flowset.control.test_support.ui.view.batch.BatchStatisticsDataDetailDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.ACTIVE_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Active tab in Batch list view")
public class BatchListViewActiveTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Refresh action availability")
    void givenLoggedInUser_whenOpenAllBatchListView_thenRefreshActionAvailable() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        // then
        listView.getRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openActiveBatchesGridContextMenu()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action: data in data grid is updated")
    void givenOpenedListView_whenClickRefreshOnActiveTab_thenDataUpdated() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        // then
        listView.waitUntilActiveDataLoading()
                .getActiveBatchesDataGrid()
                .shouldBe(emptyGrid);

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedBatch.bpmn")
                .startByKey("testFailedBatch", 2);

        List<String> processInstanceIds = camundaSampleDataManager.getStartedInstances("testFailedBatch");
        camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);

        listView.getRefreshBtn().click();

        listView.getActiveBatchesDataGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("List of available actions")
    void givenLoggedInUser_whenOpenAllBatchListView_thenAllActiveTabActionsAvailable() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        // then
        listView.getRefreshBtn().shouldBe(VISIBLE);

        listView.openActiveBatchesGridContextMenu()
                .shouldHave(visibleItems("Refresh"));
    }

    @Test
    @DisplayName("Link in Id column row opens Batch statistics detail dialog")
    void givenActiveBatch_whenIdLinkClicked_thenBatchStatisticsDetailDialogOpened() {
        // given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedBatch.bpmn")
                .startByKey("testFailedBatch", 2);

        List<String> processInstanceIds = camundaSampleDataManager.getStartedInstances("testFailedBatch");
        BatchDto batchDto = camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);

        String batchId = batchDto.getId();

        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        listView.selectActiveTab()
                .getActiveRowByBatchId(batchId)
                .getCellByIndex(ACTIVE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        $j(BatchStatisticsDataDetailDialog.class)
                .exists()
                .displayed()
                .getIdField()
                .shouldHave(value(batchId));
    }

    @Test
    @DisplayName("Double-clicking data grid row opens Batch statistics detail dialog")
    void givenActiveBatch_whenGridRowDoubleClicked_thenBatchStatisticsDetailDialogOpened() {
        // given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedBatch.bpmn")
                .startByKey("testFailedBatch", StartProcessDto.builder()
                                .variable("fail", new VariableValueDto("String", "testValue"))
                                .build(),
                        2);
        List<String> processInstanceIds = camundaSampleDataManager.getStartedInstances("testFailedBatch");
        BatchDto batchDto = camundaRestTestHelper.deleteInstancesAsync(camunda7, processInstanceIds);
        String batchId = batchDto.getId();

        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView();

        listView.getActiveRowByBatchId(batchId)
                .getDelegate()
                .doubleClick();

        // then
        $j(BatchStatisticsDataDetailDialog.class)
                .exists()
                .displayed()
                .getIdField()
                .shouldHave(value(batchId));
    }
}
