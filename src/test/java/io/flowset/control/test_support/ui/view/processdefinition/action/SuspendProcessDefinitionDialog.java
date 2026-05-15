/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition.action;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Suspend process definition view opened in dialog mode.
 * Source component: {@link io.flowset.control.view.processdefinition.SuspendProcessDefinitionView}
 */
@Getter
@TestView(id = "bpm_SuspendProcessDefinition")
public class SuspendProcessDefinitionDialog extends DialogWindow<SuspendProcessDefinitionDialog> {

    @TestComponent(path = "suspendBtn")
    private Button suspendBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
