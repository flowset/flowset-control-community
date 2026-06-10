/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.job;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DateTimePicker;
import io.jmix.masquerade.component.TextArea;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the Job detail view opened by route.
 * Source view: {@link io.flowset.control.view.job.JobDataDetailView}
 */
@Getter
@TestView(id = "JobData.detail")
public class JobDataDetailView extends View<JobDataDetailView> {

    @TestComponent(path = "idField")
    private TextField idField;

    @TestComponent(path = "jobTypeField")
    private TextField jobTypeField;

    @TestComponent(path = "createTimeField")
    private DateTimePicker createTimeField;

    @TestComponent(path = "priorityField")
    private TextField priorityField;

    @TestComponent(path = "dueDateField")
    private DateTimePicker dueDateField;

    @TestComponent(path = "retriesField")
    private TextField retriesField;

    @TestComponent(path = "activityField")
    private TextField activityField;

    @TestComponent(path = "failedActivityIdField")
    private TextField failedActivityIdField;

    @TestComponent(path = "processDefinitionIdField")
    private TextField processDefinitionIdField;

    @TestComponent(path = "viewProcessBtn")
    private Button viewProcessBtn;

    @TestComponent(path = "processInstanceIdField")
    private TextField processInstanceIdField;

    @TestComponent(path = "viewProcessInstanceBtn")
    private Button viewProcessInstanceBtn;

    @TestComponent(path = "exceptionMessageField")
    private TextArea exceptionMessageField;

    @TestComponent(path = "retryBtn")
    private Button retryBtn;

    @TestComponent(path = "suspendBtn")
    private Button suspendBtn;

    @TestComponent(path = "activateBtn")
    private Button activateBtn;

    @TestComponent(path = "closeAction")
    private Button closeBtn;
}
