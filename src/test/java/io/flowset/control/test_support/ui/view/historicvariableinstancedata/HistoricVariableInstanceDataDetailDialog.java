/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.historicvariableinstancedata;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DateTimePicker;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Historic variable instance detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.historicvariableinstancedata.HistoricVariableInstanceDataDetailView}
 */
@Getter
@TestView(id = "HistoricVariableInstanceData.detail")
public class HistoricVariableInstanceDataDetailDialog extends DialogWindow<HistoricVariableInstanceDataDetailDialog> {

    @TestComponent(path = "nameField")
    private TextField nameField;

    @TestComponent(path = "typeField")
    private TextField typeField;

    @TestComponent(path = "createTimeField")
    private DateTimePicker createTimeField;

    @TestComponent(path = "idField")
    private TextField idField;

    @TestComponent(path = "taskIdField")
    private TextField taskIdField;

    @TestComponent(path = "activityInstanceIdField")
    private TextField activityInstanceIdField;

    @TestComponent(path = "stateField")
    private TextField stateField;

    @TestComponent(path = "errorMessageField")
    private TextField errorMessageField;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;
}
