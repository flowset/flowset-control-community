/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.usertask;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the User task detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.usertaskdata.UserTaskDataDetailView}
 */
@Getter
@TestView(id = "bpm_UserTaskData.detail")
public class UserTaskDataDetailDialog extends DialogWindow<UserTaskDataDetailDialog> {

    @TestComponent(path = "taskDefinitionKeyField")
    private TextField taskDefinitionKeyField;

    @TestComponent(path = "nameField")
    private TextField nameField;

    @TestComponent(path = "assigneeField")
    private TextField assigneeField;

    @TestComponent(path = "priorityField")
    private TextField priorityField;

    @TestComponent(path = "taskIdField")
    private TextField taskIdField;

    @TestComponent(path = "descriptionField")
    private TextField descriptionField;

    @TestComponent(path = "viewProcessInstance")
    private Button viewProcessInstanceBtn;

    @TestComponent(path = "viewProcessDefinition")
    private Button viewProcessDefinitionBtn;

    @TestComponent(path = "reassignBtn")
    private Button reassignBtn;

    @TestComponent(path = "completeBtn")
    private Button completeBtn;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;
}
