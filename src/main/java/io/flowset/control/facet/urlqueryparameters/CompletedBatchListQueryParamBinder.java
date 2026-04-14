/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.batch.BatchData;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;

public class CompletedBatchListQueryParamBinder extends GridCustomHeaderFilterUrlQueryParametersBinder<BatchData> {
    public static final String ID_PARAM = "completedBatchId";
    public static final String TYPE_PARAM = "completedBatchType";

    protected final Runnable loadDelegate;

    public CompletedBatchListQueryParamBinder(DataGrid<BatchData> dataGrid, Runnable loadDelegate) {
        super(dataGrid);
        this.loadDelegate = loadDelegate;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        super.updateState(queryParameters);

        this.loadDelegate.run();
    }

    @Override
    protected void handleFilterApply(DataGridHeaderFilter.ApplyEvent applyEvent) {
        super.handleFilterApply(applyEvent);
        this.loadDelegate.run();
    }
}
