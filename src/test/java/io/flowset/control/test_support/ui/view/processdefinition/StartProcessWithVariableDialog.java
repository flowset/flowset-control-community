/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Start process with variable dialog opened from the Process definitions list view.
 * Source view: {@link io.flowset.control.view.startprocess.StartProcessWithVariableView}
 */
@Getter
@TestView(id = "bpm_StartProcessWithVariableView")
public class StartProcessWithVariableDialog extends DialogWindow<StartProcessWithVariableDialog> {

    @TestComponent(path = "name")
    private TextField nameField;

    @TestComponent(path = "version")
    private TextField versionField;

    @TestComponent(path = "processDefinitionId")
    private TextField processDefinitionIdField;

    @TestComponent(path = "businessKeyField")
    private TextField businessKeyField;

    @TestComponent(path = "startProcessBtn")
    private Button startProcessBtn;
}
