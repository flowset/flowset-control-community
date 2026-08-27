/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.usertask;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Complete user task confirmation dialog.
 * Source view: {@link io.flowset.control.view.taskcomplete.TaskCompleteView}
 */
@Getter
@TestView(id = "TaskCompleteView")
public class TaskCompleteDialog extends DialogWindow<TaskCompleteDialog> {

    public static final int NAME_COLUMN_INDEX = 0;
    public static final int VALUE_COLUMN_INDEX = 1;
    public static final int TYPE_COLUMN_INDEX = 2;

    @TestComponent(path = "createVariableBtn")
    private Button createVariableBtn;

    @TestComponent(path = "editVariableBtn")
    private Button editVariableBtn;

    @TestComponent(path = "removeVariableBtn")
    private Button removeVariableBtn;

    @TestComponent(path = "variablesGrid")
    private DataGrid variablesGrid;

    @TestComponent(path = "completeBtn")
    private Button completeBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
