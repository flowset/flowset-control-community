/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.deployment;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.Checkbox;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the confirmation dialog for the bulk delete deployment action.
 * Source dialog: {@link io.flowset.control.view.deploymentdata.BulkDeleteDeploymentView}
 */
@Getter
@TestView(id = "bpm_BulkDeleteDeployment")
public class BulkDeleteDeploymentDialog extends DialogWindow<BulkDeleteDeploymentDialog> {

    @TestComponent(path = "deleteProcessInstancesCheckBox")
    private Checkbox deleteProcessInstancesCheckBox;

    @TestComponent(path = "skipCustomListenersCheckBox")
    private Checkbox skipCustomListenersCheckBox;

    @TestComponent(path = "skipIOMappingsCheckBox")
    private Checkbox skipIOMappingsCheckBox;

    @TestComponent(path = "okBtn")
    private Button okBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
