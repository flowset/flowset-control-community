/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.*;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the properties panel inside the Process instance detail view's general panel.
 * Part of {@link io.flowset.control.view.processinstance.generalpanel.GeneralPanelFragment}
 */
@Getter
public class ProcessInstancePropertiesPanel extends Composite<ProcessInstancePropertiesPanel> {

    @TestComponent(path = "generalPanelIdField")
    private TextField idField;

    @TestComponent(path = "generalPanelStartTimeField")
    private DateTimePicker startTimeField;

    @TestComponent(path = "generalPanelEndTimeField")
    private DateTimePicker endTimeField;

    @TestComponent(path = "generalPanelBusinessKeyField")
    private TextField businessKeyField;

    @TestComponent(path = "generalPanelProcessDefinitionField")
    private TextField processDefinitionField;

    @TestComponent(path = "generalPanelOpenProcessDefinitionEditorBtn")
    private Button openProcessDefinitionEditorBtn;

    @TestComponent(path = "generalPanelRootProcessInstanceIdField")
    private TextField rootProcessInstanceIdField;

    @TestComponent(path = "generalPanelOpenRootProcessInstanceEditorBtn")
    private Button openRootProcessInstanceEditorBtn;

    @TestComponent(path = "generalPanelSuperProcessInstanceIdField")
    private TextField superProcessInstanceIdField;

    @TestComponent(path = "generalPanelOpenSuperProcessInstanceEditorBtn")
    private Button openSuperProcessInstanceEditorBtn;

    @TestComponent(path = "generalPanelDeleteReasonField")
    private TextArea deleteReasonField;

    @TestComponent(path = "generalPanelExternallyTerminatedField")
    private Checkbox externallyTerminatedField;
}
