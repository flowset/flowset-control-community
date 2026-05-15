/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.usertask;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Complete user task confirmation dialog.
 * Source view: {@link io.flowset.control.view.taskcomplete.TaskCompleteView}
 */
@Getter
@TestView(id = "TaskCompleteView")
public class TaskCompleteDialog extends DialogWindow<TaskCompleteDialog> {

    @TestComponent(path = "completeBtn")
    private Button completeBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
