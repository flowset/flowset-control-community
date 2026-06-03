/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.usertask;

import io.flowset.control.test_support.ui.UiTestSupport;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the User tasks list view.
 * Source view: {@link io.flowset.control.view.alltasks.AllTasksView}
 */
@Getter
@TestView(id = "bpm_AllTasksView")
public class AllTasksListView extends View<AllTasksListView> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");
    public static final By PROCESS_INSTANCE_BUTTON_BY = byPath("root", "idBtn");
    public static final By PROCESS_BUTTON_BY = byPath("root", "idBtn");

    public static final int CHECKBOX_COLUMN_INDEX = 0;
    public static final int TASK_ID_COLUMN_INDEX = 1;
    public static final int TASK_DEFINITION_KEY_COLUMN_INDEX = 2;
    public static final int NAME_COLUMN_INDEX = 3;
    public static final int PROCESS_INSTANCE_ID_COLUMN_INDEX = 4;
    public static final int PROCESS_DEFINITION_ID_COLUMN_INDEX = 5;
    public static final int CREATE_TIME_COLUMN_INDEX = 6;
    public static final int ASSIGNEE_COLUMN_INDEX = 7;

    @TestComponent(path = "filterBtn")
    private Button filterBtn;

    @TestComponent(path = "applyFilterBtn")
    private Button applyFilterBtn;

    @TestComponent(path = "clearBtn")
    private Button clearBtn;

    @TestComponent(path = "refreshBtn")
    private Button refreshBtn;

    @TestComponent(path = "completeTaskBtn")
    private Button completeTaskBtn;

    @TestComponent(path = "reassignTaskBtn")
    private Button reassignTaskBtn;

    @TestComponent(path = "tasksDataGrid")
    private DataGrid tasksDataGrid;

    public AllTasksListView waitUntilDataLoading() {
        UiTestSupport.waitUntilDataLoading(tasksDataGrid);
        return this;
    }

    public GridContextMenu openTasksGridContextMenu() {
        return openGridContextMenu(tasksDataGrid);
    }

    /**
     * Find the first row in the task grid related to the user task with the specified name.
     *
     * @param taskName a user task name
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByTaskName(String taskName) {
        return getRowByCellContent(tasksDataGrid, NAME_COLUMN_INDEX, taskName);
    }

    /**
     * Find the row in the task grid related to the user task with the specified id.
     *
     * @param taskId user task id
     * @return found grid row
     */
    public DataGrid.Row getRowByTaskId(String taskId) {
        return getRowByCellContent(tasksDataGrid, TASK_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(taskId);
        });
    }

    /**
     * Opens the detail view for the user task with the specified id.
     *
     * @param taskId user task id
     * @return opened detail view
     */
    public UserTaskDataDetailDialog openDetailView(String taskId) {
        DataGrid.Row rowByCellContent = getRowByCellContent(tasksDataGrid, TASK_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(taskId);
        });

        rowByCellContent.getCellByIndex(TASK_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .click();

        return $j(UserTaskDataDetailDialog.class)
                .exists()
                .displayed();
    }
}
