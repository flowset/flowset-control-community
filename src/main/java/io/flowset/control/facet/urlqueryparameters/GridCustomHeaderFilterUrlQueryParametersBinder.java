/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A binder class designed to synchronize the custom filter components in the headers of a DataGrid
 * with URL query parameters. This allows the filters applied in the grid to be reflected in
 * the URL query parameters and vice versa.
 *
 * @param <V> the type of the data grid's data items
 */
public class GridCustomHeaderFilterUrlQueryParametersBinder<V> extends ControlAbstractUrlQueryParametersBinder {
    protected final List<HasFilterUrlParamHeaderFilter> urlParamHeaderFilters;

    public GridCustomHeaderFilterUrlQueryParametersBinder(DataGrid<V> dataGrid) {
        super();
        this.urlParamHeaderFilters = initFilterUrlParametersFilters(dataGrid);
    }

    @Override
    public Component getComponent() {
        return null;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        updateFilterValues(queryParameters);
    }

    protected List<HasFilterUrlParamHeaderFilter> initFilterUrlParametersFilters(DataGrid<V> dataGrid) {
        List<HasFilterUrlParamHeaderFilter> filters = new ArrayList<>();
        for (Grid.Column<V> column : dataGrid.getColumns()) {
            Component headerComponent = column.getHeaderComponent();
            if (headerComponent != null) {
                if (headerComponent instanceof DataGridHeaderFilter headerFilter) {
                    headerFilter.addApplyListener(this::handleFilterApply);
                }
            }

            if (headerComponent instanceof HasFilterUrlParamHeaderFilter headerFilter) {
                filters.add(headerFilter);
            }
        }

        return filters;
    }

    protected void handleFilterApply(DataGridHeaderFilter.ApplyEvent applyEvent) {
        DataGridHeaderFilter source = applyEvent.getSource();
        if (source instanceof HasFilterUrlParamHeaderFilter urlParamHeaderFilter) {
            Map<String, String> paramValues = urlParamHeaderFilter.getQueryParamValues();
            updateQueryParams(paramValues);
        }
    }

    protected void updateFilterValues(QueryParameters queryParameters) {
        urlParamHeaderFilters.forEach((headerFilter) -> {
            headerFilter.updateComponents(queryParameters);
        });
    }
}
