/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.ProcessInstanceListQueryParamBinder.STARTED_AFTER_FILTER_PARAM;
import static io.flowset.control.facet.urlqueryparameters.ProcessInstanceListQueryParamBinder.STARTED_BEFORE_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.convertLocalDateTimeParamValue;
import static io.flowset.control.view.util.FilterQueryParamUtils.getLocalDateTimeParam;
import static io.flowset.control.view.util.JsUtils.SET_DEFAULT_TIME_SCRIPT;

public class StartTimeHeaderFilter extends ProcessInstanceDataGridHeaderFilter implements HasFilterUrlParamHeaderFilter {

    private TypedDateTimePicker<LocalDateTime> startTimeAfterField;
    private TypedDateTimePicker<LocalDateTime> startTimeBeforeField;

    public StartTimeHeaderFilter(DataGrid<ProcessInstanceData> dataGrid,
                                 DataGridColumn<ProcessInstanceData> column, InstanceContainer<ProcessInstanceFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    protected Component createFilterComponent() {
        Component startTimeAfterFilter = createStartTimeAfterFilter();
        Component startTimeBeforeFilter = createStartTimeBeforeFilter();

        VerticalLayout rootLayout = uiComponents.create(VerticalLayout.class);
        rootLayout.setPadding(false);
        rootLayout.setSpacing(false);
        rootLayout.add(startTimeAfterFilter, startTimeBeforeFilter);

        return rootLayout;
    }

    @Override
    public void apply() {
        LocalDateTime startTimeBefore = startTimeBeforeField.getValue();
        ProcessInstanceFilter instanceFilter = filterDc.getItem();
        if (startTimeBefore != null) {
            ZoneId zoneId = startTimeBeforeField.getZoneId();
            ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
            instanceFilter.setStartTimeBefore(startTimeBefore.atZone(zone).toOffsetDateTime());
        } else {
            instanceFilter.setStartTimeBefore(null);
        }

        LocalDateTime startTimeAfter = startTimeAfterField.getValue();
        if (startTimeAfter != null) {
            ZoneId zoneId = startTimeAfterField.getZoneId();
            ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
            instanceFilter.setStartTimeAfter(startTimeAfter.atZone(zone).toOffsetDateTime());
        } else {
            instanceFilter.setStartTimeAfter(null);
        }

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, startTimeAfter != null
                || startTimeBefore != null);
    }

    @Override
    protected void resetFilterValues() {
        startTimeAfterField.clear();
        startTimeBeforeField.clear();
    }

    @SuppressWarnings("unchecked")
    private Component createStartTimeBeforeFilter() {
        startTimeBeforeField = uiComponents.create(TypedDateTimePicker.class);
        startTimeBeforeField.setMax(LocalDateTime.now());
        startTimeBeforeField.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        startTimeBeforeField.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        startTimeBeforeField.setLabel(messages.getMessage(ProcessInstanceFilter.class, "ProcessInstanceFilter.startTimeBefore"));
        setDefaultTime(startTimeBeforeField);

        return startTimeBeforeField;
    }

    @SuppressWarnings("unchecked")
    private Component createStartTimeAfterFilter() {
        startTimeAfterField = uiComponents.create(TypedDateTimePicker.class);
        startTimeAfterField.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        startTimeAfterField.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        startTimeAfterField.setMax(LocalDateTime.now());
        startTimeAfterField.setLabel(messages.getMessage(ProcessInstanceFilter.class, "ProcessInstanceFilter.startTimeAfter"));
        setDefaultTime(startTimeAfterField);

        return startTimeAfterField;
    }

    private void setDefaultTime(TypedDateTimePicker<LocalDateTime> dateTimePicker) {
        dateTimePicker.getElement().executeJs(SET_DEFAULT_TIME_SCRIPT);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        LocalDateTime startedBefore = getLocalDateTimeParam(queryParameters, STARTED_BEFORE_FILTER_PARAM);
        startTimeBeforeField.setTypedValue(startedBefore);

        LocalDateTime startedAfter = getLocalDateTimeParam(queryParameters, STARTED_AFTER_FILTER_PARAM);
        startTimeAfterField.setTypedValue(startedAfter);

        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();

        LocalDateTime startTimeBeforeValue = startTimeBeforeField.getValue();
        paramValues.put(STARTED_BEFORE_FILTER_PARAM, convertLocalDateTimeParamValue(startTimeBeforeValue));

        LocalDateTime startTimeAfterValue = startTimeAfterField.getValue();
        paramValues.put(STARTED_AFTER_FILTER_PARAM, convertLocalDateTimeParamValue(startTimeAfterValue));

        return paramValues;
    }
}
