/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;

import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.ProcessInstanceListQueryParamBinder.BUSINESS_KEY_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class BusinessKeyHeaderFilter extends ProcessInstanceDataGridHeaderFilter implements HasFilterUrlParamHeaderFilter {

    protected TypedTextField<String> businessKey;

    public BusinessKeyHeaderFilter(DataGrid<ProcessInstanceData> dataGrid,
                                   DataGridColumn<ProcessInstanceData> column, InstanceContainer<ProcessInstanceFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }


    @Override
    protected Component createFilterComponent() {
        return createBusinessKeyFilter();
    }

    @Override
    protected void resetFilterValues() {
        businessKey.clear();
    }

    @Override
    public void apply() {
        String value = businessKey.getTypedValue();
        filterDc.getItem().setBusinessKeyLike(value);

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, value != null);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        String paramValue = getStringParam(queryParameters, BUSINESS_KEY_FILTER_PARAM);

        businessKey.setTypedValue(paramValue);
        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put(BUSINESS_KEY_FILTER_PARAM, businessKey.getTypedValue());

        return paramValues;
    }

    protected TextField createBusinessKeyFilter() {
        businessKey = uiComponents.create(TypedTextField.class);
        businessKey.setWidthFull();
        businessKey.setMinWidth("30em");
        businessKey.setClearButtonVisible(true);
        businessKey.setLabel(messages.getMessage(ProcessInstanceFilter.class, "ProcessInstanceFilter.businessKeyLike"));
        businessKey.setPlaceholder(messages.getMessage(getClass(), "businessKey.placeholder"));

        return businessKey;
    }
}
