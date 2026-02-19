/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;

import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.DecisionInstanceListQueryParamBinder.ID_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class DecisionInstanceIdHeaderFilter
        extends ContainerDataGridHeaderFilter<DecisionInstanceFilter, HistoricDecisionInstanceShortData> implements HasFilterUrlParamHeaderFilter {

    protected TypedTextField<String> decisionInstanceIdField;

    protected final Runnable loadDelegate;

    public DecisionInstanceIdHeaderFilter(DataGrid<HistoricDecisionInstanceShortData> dataGrid,
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
        String instanceIdParamValue = getStringParam(queryParameters, ID_FILTER_PARAM);
        decisionInstanceIdField.setTypedValue(instanceIdParamValue);

        updateFilterState();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put(ID_FILTER_PARAM, decisionInstanceIdField.getTypedValue());
        return paramValues;
    }

    @Override
    protected Component createFilterComponent() {
        return createDecisionInstanceIdFilter();
    }

    @Override
    protected void resetFilterValues() {
        decisionInstanceIdField.clear();
    }

    protected void updateFilterState() {
        String value = decisionInstanceIdField.getTypedValue();
        filterDc.getItem().setDecisionInstanceId(value);
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, value != null);
    }

    protected TextField createDecisionInstanceIdFilter() {
        decisionInstanceIdField = uiComponents.create(TypedTextField.class);
        decisionInstanceIdField.setWidthFull();
        decisionInstanceIdField.setMinWidth("30em");
        decisionInstanceIdField.setClearButtonVisible(true);
        decisionInstanceIdField.setLabel(messages.getMessage(DecisionInstanceFilter.class,
                "DecisionInstanceFilter.decisionInstanceId"));
        decisionInstanceIdField.setPlaceholder(messages.getMessage(getClass(), "decisionInstanceId.placeholder"));
        return decisionInstanceIdField;
    }
}
