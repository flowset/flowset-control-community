/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.usertask.UserTaskDataDetailDialog;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the User tasks tab fragment inside the History tab of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.history.HistoryUserTasksTabFragment}
 */
@Getter
public class HistoryUserTasksTabFragment extends Composite<HistoryUserTasksTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int TASK_ID_COLUMN_INDEX = 0;
    public static final int NAME_COLUMN_INDEX = 1;
    public static final int TASK_DEFINITION_KEY_COLUMN_INDEX = 2;
    public static final int ASSIGNEE_COLUMN_INDEX = 3;
    public static final int START_TIME_COLUMN_INDEX = 4;
    public static final int END_TIME_COLUMN_INDEX = 5;
    public static final int DUE_DATE_COLUMN_INDEX = 6;

    @TestComponent(path = "historyTasksGrid")
    private DataGrid historyTasksGrid;


    /**
     * Find the row in the user tasks grid related to the user task with the specified name.
     *
     * @param taskName user task name
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByTaskName(String taskName) {
        return getRowByCellContent(historyTasksGrid, NAME_COLUMN_INDEX, taskName);
    }

    /**
     * Find the row in the user tasks grid related to the user task with the specified task definition key.
     *
     * @param taskDefinitionKey a task definition key from BPMN XML
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByTaskDefinitionKey(String taskDefinitionKey) {
        return getRowByCellContent(historyTasksGrid, TASK_DEFINITION_KEY_COLUMN_INDEX, taskDefinitionKey);
    }

    /**
     * Find the first row in the user tasks grid related to the user task with the specified id.
     *
     * @param taskId a user task identifier
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByTaskId(String taskId) {
        return getRowByCellContent(historyTasksGrid, TASK_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(taskId);
        });
    }

    /**
     * Selects the row in the grid by the specified task definition key.
     *
     * @param taskDefinitionKey a task definition key
     */
    public void selectRowByTaskDefinitionKey(String taskDefinitionKey) {
        getRowByTaskDefinitionKey(taskDefinitionKey)
                .getCellByIndex(TASK_DEFINITION_KEY_COLUMN_INDEX)
                .getCellContent()
                .shouldBe(VISIBLE)
                .click();
    }

    /**
     * Opens the detail view for the user task with the specified id.
     *
     * @param taskId a user task identifier
     * @return opened detail view
     */
    public UserTaskDataDetailDialog openDetailView(String taskId) {
        getRowByTaskId(taskId)
                .getCellByIndex(TASK_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .shouldBe(VISIBLE)
                .click();

        return $j(UserTaskDataDetailDialog.class).exists()
                .displayed();
    }

    /**
     * Opens the context menu for the user tasks grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openTasksGridContextMenu() {
        return openGridContextMenu(historyTasksGrid);
    }
}
