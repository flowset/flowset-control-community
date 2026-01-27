/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.incidentdata.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.view.incidentdata.IncidentHeaderFilter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.IncidentListQueryParamBinder.TIMESTAMP_AFTER_FILTER_PARAM;
import static io.flowset.control.facet.urlqueryparameters.IncidentListQueryParamBinder.TIMESTAMP_BEFORE_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.*;
import static io.flowset.control.view.util.FilterQueryParamUtils.convertLocalDateTimeParamValue;
import static io.flowset.control.view.util.JsUtils.SET_DEFAULT_TIME_SCRIPT;

public class IncidentTimestampHeaderFilter extends IncidentHeaderFilter implements HasFilterUrlParamHeaderFilter {

    private TypedDateTimePicker<LocalDateTime> timestampAfterField;
    private TypedDateTimePicker<LocalDateTime> timestampBeforeField;

    public IncidentTimestampHeaderFilter(Grid<IncidentData> dataGrid, DataGridColumn<IncidentData> column,
                                         InstanceContainer<IncidentFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    protected Component createFilterComponent() {
        Component dateAfterFilter = createDateAfterFilter();
        Component dateBeforeFilter = createDateBeforeFilter();

        VerticalLayout rootLayout = uiComponents.create(VerticalLayout.class);
        rootLayout.setPadding(false);
        rootLayout.setSpacing(false);
        rootLayout.add(dateAfterFilter, dateBeforeFilter);

        return rootLayout;
    }

    @Override
    public void apply() {
        IncidentFilter incidentFilter = filterDc.getItem();

        LocalDateTime dateBefore = this.timestampBeforeField.getValue();
        if (dateBefore != null) {
            ZoneId zoneId = this.timestampBeforeField.getZoneId();
            ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
            incidentFilter.setIncidentTimestampBefore(dateBefore.atZone(zone).toOffsetDateTime());
        } else {
            incidentFilter.setIncidentTimestampBefore(null);
        }

        LocalDateTime dateAfter = this.timestampAfterField.getValue();
        if (dateAfter != null) {
            ZoneId zoneId = this.timestampAfterField.getZoneId();
            ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
            incidentFilter.setIncidentTimestampAfter(dateAfter.atZone(zone).toOffsetDateTime());
        } else {
            incidentFilter.setIncidentTimestampAfter(null);
        }

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, dateAfter != null
                || dateBefore != null);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        LocalDateTime timestampBefore = getLocalDateTimeParam(queryParameters, TIMESTAMP_BEFORE_FILTER_PARAM);
        timestampBeforeField.setTypedValue(timestampBefore);

        LocalDateTime timestampAfter = getLocalDateTimeParam(queryParameters, TIMESTAMP_AFTER_FILTER_PARAM);
        timestampAfterField.setTypedValue(timestampAfter);

        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();

        paramValues.put(TIMESTAMP_BEFORE_FILTER_PARAM, convertLocalDateTimeParamValue(timestampBeforeField.getValue()));
        paramValues.put(TIMESTAMP_AFTER_FILTER_PARAM, convertLocalDateTimeParamValue(timestampAfterField.getValue()));

        return paramValues;
    }

    @Override
    protected void resetFilterValues() {
        timestampAfterField.clear();
        timestampBeforeField.clear();
    }

    @SuppressWarnings("unchecked")
    private Component createDateBeforeFilter() {
        timestampBeforeField = uiComponents.create(TypedDateTimePicker.class);
        timestampBeforeField.setMax(LocalDateTime.now());
        timestampBeforeField.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        timestampBeforeField.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        timestampBeforeField.setLabel(messages.getMessage(IncidentFilter.class, "IncidentFilter.incidentTimestampBefore"));
        setDefaultTime(timestampBeforeField);

        return timestampBeforeField;
    }

    @SuppressWarnings("unchecked")
    private Component createDateAfterFilter() {
        timestampAfterField = uiComponents.create(TypedDateTimePicker.class);
        timestampAfterField.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        timestampAfterField.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        timestampAfterField.setMax(LocalDateTime.now());
        timestampAfterField.setLabel(messages.getMessage(IncidentFilter.class, "IncidentFilter.incidentTimestampAfter"));
        setDefaultTime(timestampAfterField);

        return timestampAfterField;
    }

    private void setDefaultTime(TypedDateTimePicker<LocalDateTime> dateTimePicker) {
        dateTimePicker.getElement().executeJs(SET_DEFAULT_TIME_SCRIPT);
    }
}
