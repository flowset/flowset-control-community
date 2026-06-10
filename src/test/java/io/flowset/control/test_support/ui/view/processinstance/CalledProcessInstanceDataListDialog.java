/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Called Process Instance list view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.processinstance.CalledProcessInstanceDataListView}
 */
@Getter
@TestView(id = "bpm_CalledProcessInstanceData.list")
public class CalledProcessInstanceDataListDialog extends DialogWindow<CalledProcessInstanceDataListDialog> {

    @TestComponent(path = "processInstancesDataGrid")
    private DataGrid processInstancesDataGrid;

    @TestComponent(path = "closeButton")
    private Button closeButton;
}
