/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the process definition diagram view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.processdefinition.ProcessDefinitionDiagramView}
 */
@Getter
@TestView(id = "ProcessDefinitionDiagramView")
public class ProcessDefinitionDiagramDialog extends DialogWindow<ProcessDefinitionDiagramDialog> {

    @TestComponent(path = "keyField")
    private TextField keyField;

    @TestComponent(path = "versionField")
    private TextField versionField;
}
