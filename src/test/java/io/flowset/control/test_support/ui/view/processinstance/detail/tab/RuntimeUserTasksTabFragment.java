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
 * Wrapper for the User tasks tab fragment inside the Runtime tab of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.runtime.RuntimeUserTasksTabFragment}
 */
@Getter
public class RuntimeUserTasksTabFragment extends Composite<RuntimeUserTasksTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int TASK_ID_COLUMN_INDEX = 0;
    public static final int NAME_COLUMN_INDEX = 1;
    public static final int TASK_DEFINITION_KEY_COLUMN_INDEX = 2;
    public static final int ASSIGNEE_COLUMN_INDEX = 3;
    public static final int CREATE_TIME_COLUMN_INDEX = 4;
    public static final int DUE_DATE_COLUMN_INDEX = 5;

    @TestComponent(path = "runtimeUserTaskReassignBtn")
    private Button reassignButton;

    @TestComponent(path = "runtimeUserTasksGrid")
    private DataGrid runtimeUserTasksGrid;

    @TestComponent(path = "userTasksPagination")
    private SimplePagination pagination;

    /**
     * Find the first row in the user tasks grid related to the user task with the specified name.
     *
     * @param taskName a user task name
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByTaskName(String taskName) {
        return getRowByCellContent(runtimeUserTasksGrid, NAME_COLUMN_INDEX, taskName);
    }

    /**
     * Find the first row in the user tasks grid related to the user task with the specified id.
     *
     * @param taskId a user task id
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByTaskId(String taskId) {
        return getRowByCellContent(runtimeUserTasksGrid, TASK_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(taskId);
        });
    }

    /**
     * Selects a row in the data grid for a user task with the specified name.
     *
     * @param taskName a user task name
     */
    public void selectRowByTaskName(String taskName) {
        getRowByTaskName(taskName)
                .getCellByIndex(CREATE_TIME_COLUMN_INDEX) // use cell with non-clickable content for clicking row
                .shouldBe(VISIBLE)
                .getCellContent()
                .click();
    }

    /**
     * Selects a row in the data grid for a user task with the specified id.
     *
     * @param taskId a user task id
     */
    public void selectRowByTaskId(String taskId) {
        getRowByTaskId(taskId)
                .getCellByIndex(CREATE_TIME_COLUMN_INDEX) // use cell with non-clickable content for clicking row
                .shouldBe(VISIBLE)
                .getCellContent()
                .click();
    }

    /**
     * Opens the context menu for the user tasks grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openTasksGridContextMenu() {
        return openGridContextMenu(runtimeUserTasksGrid);
    }
}
