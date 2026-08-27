/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.component.SimplePagination;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Variables tab fragment inside the History tab of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.history.HistoryVariablesTabFragment}
 */
@Getter
public class HistoryVariablesTabFragment extends Composite<HistoryVariablesTabFragment> {

    public static final By NAME_BUTTON_BY = byPath("root", "nameBtn");

    public static final int NAME_COLUMN_INDEX = 0;
    public static final int TYPE_COLUMN_INDEX = 1;
    public static final int VALUE_COLUMN_INDEX = 2;
    public static final int CREATE_TIME_COLUMN_INDEX = 3;

    @TestComponent(path = "historicVariableInstancesGrid")
    private DataGrid historicVariableInstancesGrid;

    @TestComponent(path = "historicVariableInstancesPagination")
    private SimplePagination pagination;

    /**
     * Finds the row in the grid for a variable related to the specified variable name.
     *
     * @param variableName variable name
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByVariableName(String variableName) {
        return getRowByCellContent(historicVariableInstancesGrid, NAME_COLUMN_INDEX, cell -> {
            String nameBtnText = cell.getCellContent()
                    .find(NAME_BUTTON_BY)
                    .getText();
            return nameBtnText.equals(variableName);
        });
    }

    /**
     * Selects the row in the grid by the specified variable name.
     *
     * @param variableName a variable name
     */
    public void selectRowByVariableName(String variableName) {
        getRowByVariableName(variableName)
                .getCellByIndex(TYPE_COLUMN_INDEX) // first column is Name column with link, use non-clickable cell for click
                .getCellContent()
                .shouldBe(VISIBLE)
                .click();
    }

    /**
     * Opens the context menu for the variables grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openVariablesGridContextMenu() {
        return openGridContextMenu(historicVariableInstancesGrid);
    }
}
