/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.decisiondefinition;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Decision Definition Diagram view opened in the dialog mode.
 * Source view: {@link io.flowset.control.view.decisiondefinition.DecisionDefinitionDiagramView}
 */
@Getter
@TestView(id = "DecisionDefinitionDiagramView")
public class DecisionDefinitionDiagramDialog extends DialogWindow<DecisionDefinitionDiagramDialog> {

    @TestComponent(path = "keyField")
    private TextField keyField;

    @TestComponent(path = "versionField")
    private TextField versionField;
}
