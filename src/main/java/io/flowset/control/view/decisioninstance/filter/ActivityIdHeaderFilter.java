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

import static io.flowset.control.facet.urlqueryparameters.DecisionInstanceListQueryParamBinder.ACTIVITY_ID_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class ActivityIdHeaderFilter
        extends ContainerDataGridHeaderFilter<DecisionInstanceFilter, HistoricDecisionInstanceShortData> implements HasFilterUrlParamHeaderFilter {

    protected TypedTextField<String> activityIdField;
    protected final Runnable loadDelegate;

    public ActivityIdHeaderFilter(DataGrid<HistoricDecisionInstanceShortData> dataGrid,
                                  DataGridColumn<HistoricDecisionInstanceShortData> column,
                                  InstanceContainer<DecisionInstanceFilter> filterDc, Runnable loadDelegate) {
        super(dataGrid, column, filterDc);
        this.loadDelegate = loadDelegate;
    }

    @Override
    public void apply() {
        String value = activityIdField.getTypedValue();
        filterDc.getItem().setActivityId(value);
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, value != null);

        this.loadDelegate.run();
    }

    @Override
    protected Component createFilterComponent() {
        return createActivityIdFilter();
    }

    @Override
    protected void resetFilterValues() {
        activityIdField.clear();
    }

    protected TextField createActivityIdFilter() {
        activityIdField = uiComponents.create(TypedTextField.class);
        activityIdField.setWidthFull();
        activityIdField.setMinWidth("30em");
        activityIdField.setClearButtonVisible(true);
        activityIdField.setLabel(messages.getMessage(DecisionInstanceFilter.class,
                "DecisionInstanceFilter.activityId"));
        activityIdField.setPlaceholder(messages.getMessage(getClass(), "activityId.placeHolder"));
        return activityIdField;
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        String activityParamValue = getStringParam(queryParameters, ACTIVITY_ID_FILTER_PARAM);
        activityIdField.setTypedValue(activityParamValue);
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put(ACTIVITY_ID_FILTER_PARAM, activityIdField.getTypedValue());
        return paramValues;
    }
}
