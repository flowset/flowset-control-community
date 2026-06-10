/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.component.SimplePagination;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Jobs tab fragment inside the Runtime tab of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.runtime.JobsTabFragment}
 */
@Getter
public class JobsTabFragment extends Composite<JobsTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int JOB_ID_COLUMN_INDEX = 0;
    public static final int FAILED_ACTIVITY_ID_COLUMN_INDEX = 1;
    public static final int CREATE_TIME_COLUMN_INDEX = 2;
    public static final int RETRIES_COLUMN_INDEX = 3;
    public static final int PRIORITY_COLUMN_INDEX = 4;
    public static final int STATE_COLUMN_INDEX = 5;

    @TestComponent(path = "runtimeJobRetryBtn")
    private Button retryButton;

    @TestComponent(path = "runtimeJobActivateBtn")
    private Button activateButton;

    @TestComponent(path = "runtimeJobSuspendBtn")
    private Button suspendButton;

    @TestComponent(path = "runtimeJobsGrid")
    private DataGrid runtimeJobsGrid;

    @TestComponent(path = "jobsPagination")
    private SimplePagination pagination;

    /**
     * Finds the row for a job related to the specified failed activity in the grid.
     *
     * @param failedActivityId failed activity identifier from BPMN XML
     * @return found grid row or exception thrown if not found
     */
    public DataGrid.Row getRowByFailedActivityId(String failedActivityId) {
        return getRowByCellContent(runtimeJobsGrid, FAILED_ACTIVITY_ID_COLUMN_INDEX, failedActivityId);
    }

    /**
     * Finds the row for a job with the specified identifier in the grid.
     *
     * @param jobId job identifier
     * @return found grid row or exception thrown if not found
     */
    public DataGrid.Row getRowByJobId(String jobId) {
        return getRowByCellContent(runtimeJobsGrid, JOB_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(jobId);
        });
    }

    /**
     * Selects the row in the grid related to the job with the specified identifier.
     *
     * @param jobId a job identifier
     */
    public void selectRowByJobId(String jobId) {
        getRowByJobId(jobId)
                .getCellByIndex(CREATE_TIME_COLUMN_INDEX) // first column is ID column opens a view, use non-clickable cell for click
                .getCellContent()
                .shouldBe(VISIBLE)
                .click();
    }

    /**
     * Opens the context menu for the jobs grid.
     *
     * @return grid context menu
     */
    public GridContextMenu openJobsGridContextMenu() {
        return openGridContextMenu(runtimeJobsGrid);
    }
}
