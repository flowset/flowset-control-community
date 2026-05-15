/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition.action;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the bulk activate process definition view.
 * Source component: {@link io.flowset.control.view.processdefinition.BulkActivateProcessDefinitionView}
 */
@Getter
@TestView(id = "bpm_BulkActivateProcessDefinition")
public class BulkActivateProcessDefinitionView extends View<BulkActivateProcessDefinitionView> {

    @TestComponent(path = "activateBtn")
    private Button activateBtn;

    @TestComponent(path = "cancelBtn")
    private Button cancelBtn;
}
