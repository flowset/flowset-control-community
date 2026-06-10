/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.jmix.masquerade.JConditions.VISIBLE;

/**
 * Wrapper for the Start process with variable dialog opened from the Process definitions list view.
 * Source view: {@link io.flowset.control.view.startprocess.StartProcessWithVariableView}
 */
@Getter
@TestView(id = "bpm_StartProcessWithVariableView")
public class StartProcessWithVariableDialog extends DialogWindow<StartProcessWithVariableDialog> {

    public static final int NAME_COLUMN_INDEX = 0;
    public static final int VALUE_COLUMN_INDEX = 1;
    public static final int TYPE_COLUMN_INDEX = 2;

    @TestComponent(path = "name")
    private TextField nameField;

    @TestComponent(path = "version")
    private TextField versionField;

    @TestComponent(path = "processDefinitionId")
    private TextField processDefinitionIdField;

    @TestComponent(path = "businessKeyField")
    private TextField businessKeyField;

    @TestComponent(path = "createVariableBtn")
    private Button createVariableBtn;

    @TestComponent(path = "editVariableBtn")
    private Button editVariableBtn;

    @TestComponent(path = "removeVariableBtn")
    private Button removeVariableBtn;

    @TestComponent(path = "variableGrid")
    private DataGrid variableGrid;

    @TestComponent(path = "startProcessBtn")
    private Button startProcessBtn;

    /**
     * Finds the row in the variable grid by variable name.
     * @param variableName variable name
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByVariableName(String variableName) {
        return getRowByCellContent(variableGrid, NAME_COLUMN_INDEX, variableName);
    }

    public void selectRowByVariableName(String variableName) {
        getRowByVariableName(variableName)
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .shouldBe(VISIBLE)
                .click();
    }
}
