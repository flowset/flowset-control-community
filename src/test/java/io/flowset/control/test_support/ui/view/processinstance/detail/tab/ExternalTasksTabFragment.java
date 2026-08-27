/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.flowset.control.test_support.ui.component.GridContextMenu;
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
 * Wrapper for the External tasks tab fragment inside the Runtime tab of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.runtime.ExternalTasksTabFragment}
 */
@Getter
public class ExternalTasksTabFragment extends Composite<ExternalTasksTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int EXTERNAL_TASK_ID_COLUMN_INDEX = 0;
    public static final int ACTIVITY_ID_COLUMN_INDEX = 1;
    public static final int TOPIC_NAME_COLUMN_INDEX = 2;
    public static final int RETRIES_COLUMN_INDEX = 3;
    public static final int LOCK_EXPIRATION_TIME_COLUMN_INDEX = 4;

    @TestComponent(path = "runtimeExternalTaskRetryBtn")
    private Button retryButton;

    @TestComponent(path = "runtimeExternalTasksGrid")
    private DataGrid runtimeExternalTasksGrid;

    /**
     * Finds the row in the grid for an external task related to the specified activity.
     *
     * @param activityId an activity identifier
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByActivityId(String activityId) {
        return getRowByCellContent(runtimeExternalTasksGrid, ACTIVITY_ID_COLUMN_INDEX, activityId);
    }

    /**
     * Opens the context menu for the external tasks data grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openTasksGridContextMenu() {
        return openGridContextMenu(runtimeExternalTasksGrid);
    }

    /**
     * Finds the row in the grid for an external task with the specified external task identifier.
     *
     * @param externalTaskId an external task identifier
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByExternalTaskId(String externalTaskId) {
        return getRowByCellContent(runtimeExternalTasksGrid, EXTERNAL_TASK_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(externalTaskId);
        });
    }

    /**
     * Selects the row in the grid by the specified external task identifier.
     *
     * @param externalTaskId an external task identifier
     */
    public void selectRowByExternalTaskId(String externalTaskId) {
        getRowByExternalTaskId(externalTaskId)
                .getCellByIndex(TOPIC_NAME_COLUMN_INDEX) // use non-clickable cell for click
                .getCellContent()
                .shouldBe(VISIBLE)
                .click();
    }
}
