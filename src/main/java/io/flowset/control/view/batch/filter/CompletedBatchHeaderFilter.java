/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.filter;

import com.vaadin.flow.component.grid.Grid;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;

public abstract class CompletedBatchHeaderFilter extends ContainerDataGridHeaderFilter<BatchFilter, BatchData> {
    public CompletedBatchHeaderFilter(Grid<BatchData> dataGrid,
                                      DataGridColumn<BatchData> column,
                                      InstanceContainer<BatchFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }
}
