/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.action;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Terminate process instance confirmation dialog.
 * Source view: {@link io.flowset.control.view.processinstanceterminate.ProcessInstanceTerminateView}
 */
@Getter
@TestView(id = "ProcessInstanceTerminateView")
public class ProcessInstanceTerminateDialog extends DialogWindow<ProcessInstanceTerminateDialog> {

    @TestComponent(path = "terminateBtn")
    private Button terminateBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
