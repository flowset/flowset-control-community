/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentdata.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.view.incidentdata.IncidentHeaderFilter;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceContainer;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.IncidentListQueryParamBinder.ACTIVITY_ID_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class ActivityHeaderFilter extends IncidentHeaderFilter implements HasFilterUrlParamHeaderFilter {

    protected TypedTextField<String> activityIdField;

    public ActivityHeaderFilter(Grid<IncidentData> dataGrid,
                                DataGridColumn<IncidentData> column,
                                InstanceContainer<IncidentFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    protected Component createFilterComponent() {
        return createActivityFilter();
    }

    @Override
    protected void resetFilterValues() {
        activityIdField.clear();
    }

    @Override
    public void apply() {
        String value = activityIdField.getTypedValue();
        boolean emptyValue = StringUtils.isEmpty(value);
        if (emptyValue) {
            value = null;
        }
        filterDc.getItem().setActivityId(value);

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, !emptyValue);
    }

    protected TextField createActivityFilter() {
        activityIdField = uiComponents.create(TypedTextField.class);
        activityIdField.setWidthFull();
        activityIdField.setMinWidth("30em");
        activityIdField.setClearButtonVisible(true);
        activityIdField.setLabel(messages.getMessage(IncidentFilter.class, "IncidentFilter.activityId"));
        activityIdField.setPlaceholder(messages.getMessage(getClass(), "activityId.placeholder"));

        return activityIdField;
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        String paramValue = getStringParam(queryParameters, ACTIVITY_ID_FILTER_PARAM);
        activityIdField.setTypedValue(paramValue);

        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put(ACTIVITY_ID_FILTER_PARAM, activityIdField.getTypedValue());

        return paramValues;
    }
}
