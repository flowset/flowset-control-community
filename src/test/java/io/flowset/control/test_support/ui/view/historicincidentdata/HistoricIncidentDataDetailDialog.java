/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.historicincidentdata;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.*;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Historic incident detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.historicincidentdata.HistoricIncidentDataDetailView}
 */
@Getter
@TestView(id = "HistoricIncidentData.detail")
public class HistoricIncidentDataDetailDialog extends DialogWindow<HistoricIncidentDataDetailDialog> {

    @TestComponent(path = "incidentIdField")
    private TextField incidentIdField;

    @TestComponent(path = "createTimeField")
    private DateTimePicker createTimeField;

    @TestComponent(path = "endTimeField")
    private DateTimePicker endTimeField;

    @TestComponent(path = "resolvedField")
    private Checkbox resolvedField;

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

    @TestComponent(path = "causeIncidentIdField")
    private TextField causeIncidentIdField;

    @TestComponent(path = "rootCauseIncidentIdField")
    private TextField rootCauseIncidentIdField;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;
}
