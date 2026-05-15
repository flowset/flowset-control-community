/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.incident;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DateTimePicker;
import io.jmix.masquerade.component.TextArea;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

/**
 * Wrapper for the Incident detail view opened as a route view.
 * Source view: {@link io.flowset.control.view.incidentdata.IncidentDataDetailView}
 */
@Getter
@TestView(id = "IncidentData.detail")
public class IncidentDataDetailView extends View<IncidentDataDetailView> {

    @TestComponent(path = "incidentIdField")
    private TextField incidentIdField;

    @TestComponent(path = "timestampField")
    private DateTimePicker timestampField;

    @TestComponent(path = "typeField")
    private TextField typeField;

    @TestComponent(path = "messageField")
    private TextArea messageField;

    @TestComponent(path = "configurationField")
    private TextField configurationField;

    @TestComponent(path = "processInstanceIdField")
    private TextField processInstanceIdField;

    @TestComponent(path = "processDefinitionIdField")
    private TextField processDefinitionIdField;

    @TestComponent(path = "activityIdField")
    private TextField activityIdField;

    @TestComponent(path = "failedActivityIdField")
    private TextField failedActivityIdField;

    @TestComponent(path = "causeIncidentIdField")
    private TextField causeIncidentIdField;

    @TestComponent(path = "rootCauseIncidentIdField")
    private TextField rootCauseIncidentIdField;

    @TestComponent(path = "copyIdBtn")
    private Button copyIdBtn;

    @TestComponent(path = "viewStacktraceBtn")
    private Button viewStacktraceBtn;

    @TestComponent(path = "configurationBtn")
    private Button configurationBtn;

    @TestComponent(path = "viewProcessInstanceBtn")
    private Button viewProcessInstanceBtn;

    @TestComponent(path = "viewProcessBtn")
    private Button viewProcessBtn;

    @TestComponent(path = "viewCauseIncidentBtn")
    private Button viewCauseIncidentBtn;

    @TestComponent(path = "viewRootCauseIncidentBtn")
    private Button viewRootCauseIncidentBtn;

    @TestComponent(path = "retryBtn")
    private Button retryBtn;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;
}
