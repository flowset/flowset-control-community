/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.batch.list;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.BatchDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.batch.AllBatchListView;
import io.flowset.control.test_support.ui.view.batch.BatchDataDetailDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static io.flowset.control.test_support.ui.condition.ControlCondition.emptyGrid;
import static io.flowset.control.test_support.ui.condition.ControlCondition.visibleBodyRowCount;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.COMPLETED_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.batch.AllBatchListView.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Actions on Completed tab in Batch list view")
public class BatchListViewCompletedTabActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Refresh action availability on Completed tab")
    void givenOpenedListView_whenSwitchToCompletedTab_thenRefreshActionAvailable() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        // then
        listView.getCompletedBatchRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        listView.openCompletedBatchesGridContextMenu()
                .find(text("Refresh"))
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action: data in Completed tab data grid is updated")
    void givenOpenedListView_whenClickRefreshOnCompletedTab_thenDataUpdated() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        // then
        listView.waitUntilCompletedDataLoading()
                .getCompletedBatchesDataGrid()
                .shouldBe(emptyGrid);

        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.waitForBatchExecution(camunda7);

        listView.getCompletedBatchRefreshBtn().click();

        listView.getCompletedBatchesDataGrid()
                .shouldHave(visibleBodyRowCount(1));
    }

    @Test
    @DisplayName("List of available actions on Completed tab")
    void givenCompletedBatch_whenSwitchToCompletedTab_thenAllCompletedTabActionsAvailable() {
        // given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.waitForBatchExecution(camunda7);

        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        // then
        listView.getCompletedBatchRefreshBtn().shouldBe(VISIBLE);

        listView.openCompletedBatchesGridContextMenu()
                .shouldHave(visibleItems("Refresh"));
    }

    @Test
    @DisplayName("ID link in Completed tab row opens Batch data detail dialog")
    void givenCompletedBatch_whenIdLinkClicked_thenBatchDataDetailDialogOpened() {
        // given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batch = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batch.getId();

        camundaRestTestHelper.waitForBatchExecution(camunda7);

        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        listView.getCompletedRowByBatchId(batchId)
                .getCellByIndex(COMPLETED_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY).click();

        // then
        $j(BatchDataDetailDialog.class)
                .exists()
                .displayed()
                .getIdField()
                .shouldHave(value(batchId));
    }

    @Test
    @DisplayName("Double-clicking a Completed tab row opens Batch data detail dialog")
    void givenCompletedBatch_whenRowDoubleClicked_thenBatchDataDetailDialogOpened() {
        // given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batch = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batch.getId();

        camundaRestTestHelper.waitForBatchExecution(camunda7);

        MainView mainView = loginAsAdmin();

        // when
        AllBatchListView listView = mainView.openAllBatchListView()
                .selectCompletedTab();

        listView.getCompletedRowByBatchId(batchId)
                .getDelegate()
                .doubleClick();

        // then
        $j(BatchDataDetailDialog.class)
                .exists()
                .displayed()
                .getIdField()
                .shouldHave(value(batchId));
    }
}
