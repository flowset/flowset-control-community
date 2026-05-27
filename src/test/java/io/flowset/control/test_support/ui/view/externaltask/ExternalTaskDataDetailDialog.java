/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.externaltask;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextArea;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the External task detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.externaltask.ExternalTaskDataDetailView}
 */
@Getter
@TestView(id = "ExternalTaskData.detail")
public class ExternalTaskDataDetailDialog extends DialogWindow<ExternalTaskDataDetailDialog> {

    @TestComponent(path = "externalTaskIdField")
    private TextField externalTaskIdField;

    @TestComponent(path = "copyIdBtn")
    private Button copyIdBtn;

    @TestComponent(path = "topicNameField")
    private TextField topicNameField;

    @TestComponent(path = "retriesField")
    private TextField retriesField;

    @TestComponent(path = "priorityField")
    private TextField priorityField;

    @TestComponent(path = "activityIdField")
    private TextField activityIdField;

    @TestComponent(path = "stateField")
    private TextField stateField;

    @TestComponent(path = "workerIdField")
    private TextField workerIdField;

    @TestComponent(path = "businessKeyField")
    private TextField businessKeyField;

    @TestComponent(path = "activityInstanceIdField")
    private TextField activityInstanceIdField;

    @TestComponent(path = "processDefinitionIdField")
    private TextField processDefinitionIdField;

    @TestComponent(path = "processInstanceIdField")
    private TextField processInstanceIdField;

    @TestComponent(path = "errorMessageField")
    private TextArea errorMessageField;

    @TestComponent(path = "copyErrorBtn")
    private Button copyErrorBtn;

    @TestComponent(path = "copyErrorDetailsBtn")
    private Button copyErrorDetailsBtn;

    @TestComponent(path = "retryBtn")
    private Button retryBtn;
}
