/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.usertask;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Reassign user task dialog.
 * Source view: {@link io.flowset.control.view.taskreassign.TaskReassignView}
 */
@Getter
@TestView(id = "bpm_TaskReassignView")
public class TaskReassignDialog extends DialogWindow<TaskReassignDialog> {

    @TestComponent(path = "newAssigneeField")
    private TextField newAssigneeField;

    @TestComponent(path = "okBtn")
    private Button okBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
