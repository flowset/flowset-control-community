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
 * Wrapper for the Activate process instance confirmation dialog.
 * Source view: {@link io.flowset.control.view.processinstance.ActivateProcessInstanceView}
 */
@Getter
@TestView(id = "ActivateProcessInstanceView")
public class ActivateProcessInstanceDialog extends DialogWindow<ActivateProcessInstanceDialog> {

    @TestComponent(path = "activateBtn")
    private Button activateBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
