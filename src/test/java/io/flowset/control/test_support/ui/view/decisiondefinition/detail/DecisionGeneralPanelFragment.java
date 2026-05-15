/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.decisiondefinition.detail;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;

/**
 * Wrapper for the general-info panel fragment shown on the Decision detail view.
 * Source component: {@link io.flowset.control.view.decisiondefinition.detail.GeneralPanelFragment}
 */
@Getter
public class DecisionGeneralPanelFragment extends Composite<DecisionGeneralPanelFragment> {

    @TestComponent(path = "generalPanelInfoBtn")
    private Button infoBtn;

    @TestComponent(path = "generalPanelUpperPanel")
    private DecisionPropertiesPanelFragment propertiesPanel;

    @TestComponent(path = "generalPanelViewDeployment")
    private Button viewDeploymentBtn;

    @TestComponent(path = "generalPanelCopyKeyButton")
    private Button copyKeyButton;

    @TestComponent(path = "generalPanelCopyIdButton")
    private Button copyIdButton;

    @TestComponent(path = "generalPanelKeyField")
    private TextField keyField;

    @TestComponent(path = "generalPanelIdField")
    private TextField idField;

    @TestComponent(path = "generalPanelDeploymentIdField")
    private TextField deploymentIdField;
}
