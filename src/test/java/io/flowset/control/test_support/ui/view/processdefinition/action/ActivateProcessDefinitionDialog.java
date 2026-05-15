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
 * Wrapper for the Activate process confirmation dialog.
 * Source component: {@link io.flowset.control.view.processdefinition.ActivateProcessDefinitionView}
 */
@Getter
@TestView(id = "bpm_ActivateProcessDefinition")
public class ActivateProcessDefinitionDialog extends DialogWindow<ActivateProcessDefinitionDialog> {

    @TestComponent(path = "activateBtn")
    private Button activateBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
