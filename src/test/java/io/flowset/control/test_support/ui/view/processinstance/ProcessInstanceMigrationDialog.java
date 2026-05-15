/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.ComboBox;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Migrate process instance dialog.
 * Source view: {@link io.flowset.control.view.processinstancemigration.ProcessInstanceMigrationView}
 */
@Getter
@TestView(id = "bpm_ProcessInstanceMigration")
public class ProcessInstanceMigrationDialog extends DialogWindow<ProcessInstanceMigrationDialog> {

    @TestComponent(path = "sourceDefinitionKeyField")
    private TextField sourceDefinitionKeyField;

    @TestComponent(path = "sourceDefinitionVersionField")
    private TextField sourceDefinitionVersionField;

    @TestComponent(path = "processDefinitionVersionComboBox")
    private ComboBox processDefinitionVersionComboBox;

    @TestComponent(path = "migrateBtn")
    private Button migrateBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
