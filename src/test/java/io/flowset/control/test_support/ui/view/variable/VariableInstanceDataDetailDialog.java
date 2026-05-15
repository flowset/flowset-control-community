/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.variable;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.ComboBox;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.JSelectors.byUiTestId;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the Variable instance detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.processvariable.VariableInstanceDataDetail}
 */
@Getter
@TestView(id = "bpm_VariableInstanceData.detail")
public class VariableInstanceDataDetailDialog extends DialogWindow<VariableInstanceDataDetailDialog> {

    @TestComponent(path = "nameField")
    private TextField nameField;

    @TestComponent(path = "typeComboBox")
    private ComboBox typeComboBox;

    @TestComponent(path = "saveBtn")
    private Button saveBtn;

    @TestComponent(path = "okBtn")
    private Button okBtn;

    /**
     * Returns the value component as the given wrapper class.
     *
     * @param wrapperClass component wrapper class
     * @param <T>          component wrapper type
     * @return component wrapper instance
     */
    public <T> T getValueComponentAs(Class<T> wrapperClass) {
        return $j(wrapperClass, byChained(getBy(), byUiTestId("valueForm"), byUiTestId("variableValueField")));
    }
}
