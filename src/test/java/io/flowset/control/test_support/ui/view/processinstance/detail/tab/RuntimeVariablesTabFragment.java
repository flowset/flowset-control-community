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
 * Wrapper for the Variables tab fragment inside the Runtime tab of the Process instance detail view.
 * Part of {@link io.flowset.control.view.processinstance.runtime.RuntimeTabFragment}
 */
@Getter
public class RuntimeVariablesTabFragment extends Composite<RuntimeVariablesTabFragment> {

    public static final By NAME_BUTTON_BY = byPath("root", "nameBtn");
    public static final int CHECKBOX_COLUMN_INDEX = 0;
    public static final int NAME_COLUMN_INDEX = 1;
    public static final int TYPE_COLUMN_INDEX = 2;
    public static final int VALUE_COLUMN_INDEX = 3;
    public static final int SCOPE_COLUMN_INDEX = 4;

    @TestComponent(path = "runtimeVariableCreateBtn")
    private Button createButton;

    @TestComponent(path = "runtimeVariableRemoveBtn")
    private Button removeButton;

    @TestComponent(path = "runtimeVariablesGrid")
    private DataGrid runtimeVariablesGrid;

    /**
     * Find the first row in the variable grid related to the variable with the specified name.
     *
     * @param variableName a variable name
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByVariableName(String variableName) {
        return getRowByCellContent(runtimeVariablesGrid, NAME_COLUMN_INDEX, cell -> {
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
                .getCellByIndex(CHECKBOX_COLUMN_INDEX) // multiple select enabled: select using checkbox
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
        return openGridContextMenu(runtimeVariablesGrid);
    }
}
