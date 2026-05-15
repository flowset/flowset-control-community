/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.historicactivityinstancedata;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DateTimePicker;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Historic activity instance detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.historicactivityinstancedata.HistoricActivityInstanceDataDetailView}
 */
@Getter
@TestView(id = "HistoricActivityInstanceData.detail")
public class HistoricActivityInstanceDataDetailDialog extends DialogWindow<HistoricActivityInstanceDataDetailDialog> {

    @TestComponent(path = "activityInstanceIdField")
    private TextField activityInstanceIdField;

    @TestComponent(path = "activityIdField")
    private TextField activityIdField;

    @TestComponent(path = "activityNameField")
    private TextField activityNameField;

    @TestComponent(path = "activityTypeField")
    private TextField activityTypeField;

    @TestComponent(path = "startTimeField")
    private DateTimePicker startTimeField;

    @TestComponent(path = "endTimeField")
    private DateTimePicker endTimeField;

    @TestComponent(path = "durationField")
    private TextField durationField;

    @TestComponent(path = "assigneeField")
    private TextField assigneeField;

    @TestComponent(path = "taskIdField")
    private TextField taskIdField;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;
}
