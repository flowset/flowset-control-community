/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;

import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.DecisionInstanceListQueryParamBinder.PROCESS_INSTANCE_ID_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class ProcessInstanceIdHeaderFilter
        extends ContainerDataGridHeaderFilter<DecisionInstanceFilter, HistoricDecisionInstanceShortData> implements HasFilterUrlParamHeaderFilter {

    protected TypedTextField<String> processInstanceIdField;

    protected final Runnable loadDelegate;

    public ProcessInstanceIdHeaderFilter(DataGrid<HistoricDecisionInstanceShortData> dataGrid,
                                         DataGridColumn<HistoricDecisionInstanceShortData> column,
                                         InstanceContainer<DecisionInstanceFilter> filterDc, Runnable loadDelegate) {
        super(dataGrid, column, filterDc);
        this.loadDelegate = loadDelegate;
    }

    @Override
    public void apply() {
        updateFilterState();

        this.loadDelegate.run();
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        String instanceIdParamValue = getStringParam(queryParameters, PROCESS_INSTANCE_ID_FILTER_PARAM);
        processInstanceIdField.setTypedValue(instanceIdParamValue);

        updateFilterState();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put(PROCESS_INSTANCE_ID_FILTER_PARAM, processInstanceIdField.getTypedValue());
        return paramValues;
    }

    @Override
    protected Component createFilterComponent() {
        return createProcessInstanceIdFilter();
    }

    @Override
    protected void resetFilterValues() {
        processInstanceIdField.clear();
    }

    protected void updateFilterState() {
        String value = processInstanceIdField.getTypedValue();
        filterDc.getItem().setProcessInstanceId(value);
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, value != null);
    }

    protected TextField createProcessInstanceIdFilter() {
        processInstanceIdField = uiComponents.create(TypedTextField.class);
        processInstanceIdField.setWidthFull();
        processInstanceIdField.setMinWidth("30em");
        processInstanceIdField.setClearButtonVisible(true);
        processInstanceIdField.setLabel(messages.getMessage(DecisionInstanceFilter.class,
                "DecisionInstanceFilter.processInstanceId"));
        processInstanceIdField.setPlaceholder(messages.getMessage(getClass(), "processInstanceId.placeHolder"));
        return processInstanceIdField;
    }
}
