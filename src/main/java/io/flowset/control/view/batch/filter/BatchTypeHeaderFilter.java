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

import static io.flowset.control.facet.urlqueryparameters.ActiveBatchListQueryParamBinder.TYPE_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class BatchTypeHeaderFilter extends BatchHeaderFilter implements HasFilterUrlParamHeaderFilter {
    protected TypedTextField<String> typeField;

    public BatchTypeHeaderFilter(Grid<BatchStatisticsData> dataGrid,
                                 DataGridColumn<BatchStatisticsData> column,
                                 InstanceContainer<BatchFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    protected Component createFilterComponent() {
        typeField = uiComponents.create(TypedTextField.class);
        typeField.setWidthFull();
        typeField.setMinWidth("20em");
        typeField.setClearButtonVisible(true);
        typeField.setLabel(messages.getMessage(BatchFilter.class, "BatchFilter.type"));
        typeField.setPlaceholder(messages.getMessage(getClass(), "enterValue"));

        return typeField;
    }

    @Override
    public void apply() {
        String type = StringUtils.trimToNull(typeField.getTypedValue());
        filterDc.getItem().setType(type);

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, type != null);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        typeField.setTypedValue(getStringParam(queryParameters, TYPE_PARAM));

        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put(TYPE_PARAM, StringUtils.trimToNull(typeField.getTypedValue()));

        return paramValues;
    }

    @Override
    protected void resetFilterValues() {
        typeField.clear();
    }
}
