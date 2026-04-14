/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.ActiveBatchListQueryParamBinder.ID_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class BatchIdHeaderFilter extends BatchHeaderFilter implements HasFilterUrlParamHeaderFilter {
    protected TypedTextField<String> idField;

    public BatchIdHeaderFilter(Grid<BatchStatisticsData> dataGrid,
                               DataGridColumn<BatchStatisticsData> column,
                               InstanceContainer<BatchFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    protected Component createFilterComponent() {
        idField = uiComponents.create(TypedTextField.class);
        idField.setWidthFull();
        idField.setMinWidth("30em");
        idField.setClearButtonVisible(true);
        idField.setLabel(messages.getMessage(BatchFilter.class, "BatchFilter.batchId"));
        idField.setPlaceholder(messages.getMessage(getClass(), "enterValue"));
        return idField;
    }

    @Override
    public void apply() {
        String id = StringUtils.trimToNull(idField.getTypedValue());
        filterDc.getItem().setBatchId(id);
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, id != null);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        idField.setTypedValue(getStringParam(queryParameters, ID_PARAM));
        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put(ID_PARAM, StringUtils.trimToNull(idField.getTypedValue()));
        return paramValues;
    }

    @Override
    protected void resetFilterValues() {
        idField.clear();
    }
}
