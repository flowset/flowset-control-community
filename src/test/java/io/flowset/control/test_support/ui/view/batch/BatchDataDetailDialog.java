/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.batch;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;

/**
 * Wrapper for the Batch data detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.batch.BatchDataDetailView}
 */
@Getter
@TestView(id = "BatchData.detail")
public class BatchDataDetailDialog extends DialogWindow<BatchDataDetailDialog> {

    @TestComponent(path = "idField")
    private TextField idField;

    @TestComponent(path = "typeField")
    private TextField typeField;

    @TestComponent(path = "stateField")
    private TextField stateField;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;
}
