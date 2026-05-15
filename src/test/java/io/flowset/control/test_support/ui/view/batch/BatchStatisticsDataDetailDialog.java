/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.batch;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.DialogWindow;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.jmix.masquerade.JSelectors.byPath;
import static org.openqa.selenium.By.cssSelector;

/**
 * Wrapper for the Batch statistics detail view opened in dialog mode.
 * Source view: {@link io.flowset.control.view.batch.BatchStatisticsDetailView}
 */
@Getter
@TestView(id = "BatchStatisticsData.detail")
public class BatchStatisticsDataDetailDialog extends DialogWindow<BatchStatisticsDataDetailDialog> {

    public static final By JOB_ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int JOB_ID_COLUMN_INDEX = 0;

    @TestComponent(path = "idField")
    private TextField idField;

    @TestComponent(path = "typeField")
    private TextField typeField;

    @TestComponent(path = "stateField")
    private TextField stateField;

    @TestComponent(path = "closeBtn")
    private Button closeBtn;

    public DataGrid getJobsDataGrid() {
        return new DataGrid(cssSelector("vaadin-dialog-overlay vaadin-grid"));
    }
}
