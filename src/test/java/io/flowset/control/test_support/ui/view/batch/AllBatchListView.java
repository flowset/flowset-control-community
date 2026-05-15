/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.batch;

import io.flowset.control.test_support.ui.UiTestSupport;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.component.TabSheet;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JConditions.SELECTED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Batch list view.
 * Source view: {@link io.flowset.control.view.batch.AllBatchListView}
 */
@Getter
@TestView(id = "bpm_AllBatchListView")
public class AllBatchListView extends View<AllBatchListView> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int ACTIVE_ID_COLUMN_INDEX = 0;
    public static final int ACTIVE_TYPE_COLUMN_INDEX = 1;
    public static final int ACTIVE_START_TIME_COLUMN_INDEX = 2;
    public static final int ACTIVE_STATE_COLUMN_INDEX = 3;
    public static final int ACTIVE_FAILED_JOBS_COLUMN_INDEX = 4;
    public static final int ACTIVE_TOTAL_JOBS_COLUMN_INDEX = 5;
    public static final int ACTIVE_PROGRESS_COLUMN_INDEX = 6;

    public static final int COMPLETED_ID_COLUMN_INDEX = 0;
    public static final int COMPLETED_TYPE_COLUMN_INDEX = 1;
    public static final int COMPLETED_START_TIME_COLUMN_INDEX = 2;
    public static final int COMPLETED_END_TIME_COLUMN_INDEX = 3;
    public static final int COMPLETED_TOTAL_JOBS_COLUMN_INDEX = 4;

    @TestComponent
    private TabSheet tabsheet;

    @TestComponent(path = "refreshBtn")
    private Button refreshBtn;

    @TestComponent(path = "activeBatchesDataGrid")
    private DataGrid activeBatchesDataGrid;

    @TestComponent(path = "completedBatchRefreshBtn")
    private Button completedBatchRefreshBtn;

    @TestComponent(path = "completedBatchesDataGrid")
    private DataGrid completedBatchesDataGrid;

    /**
     * Selects the Active tab.
     *
     * @return current view
     */
    public AllBatchListView selectActiveTab() {
        tabsheet.getTabById("activeTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);
        return this;
    }

    /**
     * Selects the Completed tab.
     *
     * @return current view
     */
    public AllBatchListView selectCompletedTab() {
        tabsheet.getTabById("completedTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);
        return this;
    }

    /**
     * Waits until the Active tab data is loaded.
     *
     * @return current view
     */
    public AllBatchListView waitUntilActiveDataLoading() {
        UiTestSupport.waitUntilDataLoading(activeBatchesDataGrid);
        return this;
    }

    /**
     * Waits until the Completed tab data is loaded.
     *
     * @return current view
     */
    public AllBatchListView waitUntilCompletedDataLoading() {
        UiTestSupport.waitUntilDataLoading(completedBatchesDataGrid);
        return this;
    }

    /**
     * Finds the row in the Active tab grid related to the batch with the specified id.
     *
     * @param batchId batch id displayed in the ID column
     * @return row in the Active tab grid related to the batch with the specified id
     */
    public DataGrid.Row getActiveRowByBatchId(String batchId) {
        return getRowByCellContent(activeBatchesDataGrid, ACTIVE_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(batchId);
        });
    }

    /**
     * Finds the row in the Completed tab grid related to the batch with the specified id.
     *
     * @param batchId batch id displayed in the ID column
     * @return row in the Completed tab grid related to the batch with the specified id
     */
    public DataGrid.Row getCompletedRowByBatchId(String batchId) {
        return getRowByCellContent(completedBatchesDataGrid, COMPLETED_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(batchId);
        });
    }

    /**
     * Opens the context menu of the Active tab grid.
     *
     * @return collection of visible context menu items
     */
    public GridContextMenu openActiveBatchesGridContextMenu() {
        return openGridContextMenu(activeBatchesDataGrid);
    }

    /**
     * Opens the context menu of the Completed tab grid.
     *
     * @return collection of visible context menu items
     */
    public GridContextMenu openCompletedBatchesGridContextMenu() {
        return openGridContextMenu(completedBatchesDataGrid);
    }
}
