/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition.detail;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.*;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the process-properties panel inside the Process detail view's general panel.
 * Part of {@link io.flowset.control.view.processdefinition.GeneralPanelFragment}
 */
@Getter
public class ProcessPropertiesPanelFragment extends Composite<ProcessPropertiesPanelFragment> {

    @TestComponent(path = "generalPanelKeyField")
    private TextField keyField;

    @TestComponent(path = "generalPanelCopyKeyButton")
    private Button copyKeyButton;

    @TestComponent(path = "generalPanelIdField")
    private TextField idField;

    @TestComponent(path = "generalPanelCopyIdButton")
    private Button copyIdButton;

    @TestComponent(path = "generalPanelStartableInTaskListField")
    private Checkbox startableInTaskListField;

    @TestComponent(path = "generalPanelDescriptionField")
    private TextArea descriptionField;

    @TestComponent(path = "generalPanelDeploymentGroup")
    private Unknown deploymentGroup;

    @TestComponent(path = "generalPanelDeploymentIdField")
    private TextField deploymentIdField;

    @TestComponent(path = "generalPanelViewDeployment")
    private Button viewDeploymentBtn;

    @TestComponent(path = "generalPanelDeploymentTimeField")
    private DateTimePicker deploymentTimeField;

    @TestComponent(path = "generalPanelDeploymentSourceField")
    private TextField deploymentSourceField;
}
