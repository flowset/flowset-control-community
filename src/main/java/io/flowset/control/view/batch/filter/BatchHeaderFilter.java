/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.filter;

import com.vaadin.flow.component.grid.Grid;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;

public abstract class BatchHeaderFilter extends ContainerDataGridHeaderFilter<BatchFilter, BatchStatisticsData> {
    public BatchHeaderFilter(Grid<BatchStatisticsData> dataGrid,
                             DataGridColumn<BatchStatisticsData> column,
                             InstanceContainer<BatchFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }
}
