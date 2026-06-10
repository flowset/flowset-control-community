/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.terminatereason;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.component.Unknown;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the terminate reason dialog opened from the Process instances list view state column.
 * Source view: {@link io.flowset.control.view.processinstance.terminatereason.InstanceTerminateReasonView}
 */
@Getter
@TestView(id = "InstanceTerminateReasonView")
public class InstanceTerminateReasonDialog extends DialogWindow<InstanceTerminateReasonDialog> {

    @TestComponent(path = "instanceId")
    private TextField instanceIdField;

    @TestComponent(path = "incidentsGrid")
    private DataGrid incidentsGrid;

    @TestComponent(path = "incidentsGroup")
    private Unknown incidentsGroup;
}
