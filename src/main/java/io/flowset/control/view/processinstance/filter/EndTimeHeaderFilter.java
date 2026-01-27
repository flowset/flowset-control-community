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

import static io.flowset.control.facet.urlqueryparameters.ProcessInstanceListQueryParamBinder.END_AFTER_FILTER_PARAM;
import static io.flowset.control.facet.urlqueryparameters.ProcessInstanceListQueryParamBinder.END_BEFORE_FILTER_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getLocalDateTimeParam;
import static io.flowset.control.view.util.FilterQueryParamUtils.convertLocalDateTimeParamValue;
import static io.flowset.control.view.util.JsUtils.SET_DEFAULT_TIME_SCRIPT;

public class EndTimeHeaderFilter extends ProcessInstanceDataGridHeaderFilter implements HasFilterUrlParamHeaderFilter {

    protected TypedDateTimePicker<LocalDateTime> endTimeBeforeField;
    protected TypedDateTimePicker<LocalDateTime> endTimeAfterField;

    public EndTimeHeaderFilter(DataGrid<ProcessInstanceData> dataGrid, DataGridColumn<ProcessInstanceData> column,
                               InstanceContainer<ProcessInstanceFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }


    @Override
    protected Component createFilterComponent() {
        Component endTimeAfterFilter = createEndTimeAfterFilter();
        Component endTimeBeforeFilter = createEndTimeBeforeFilter();

        VerticalLayout rootLayout = uiComponents.create(VerticalLayout.class);
        rootLayout.setPadding(false);
        rootLayout.setSpacing(false);
        rootLayout.add(endTimeAfterFilter, endTimeBeforeFilter);

        return rootLayout;
    }


    @Override
    protected void resetFilterValues() {
        endTimeBeforeField.clear();
        endTimeAfterField.clear();
    }

    @Override
    public void apply() {
        ProcessInstanceFilter instanceFilter = filterDc.getItem();

        LocalDateTime endTimeBefore = this.endTimeBeforeField.getValue();
        if (endTimeBefore != null) {
            ZoneId zoneId = this.endTimeBeforeField.getZoneId();
            ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
            instanceFilter.setEndTimeBefore(endTimeBefore.atZone(zone).toOffsetDateTime());
        } else {
            instanceFilter.setEndTimeBefore(null);
        }

        LocalDateTime endTimeAfter = this.endTimeAfterField.getValue();
        if (endTimeAfter != null) {
            ZoneId zoneId = this.endTimeAfterField.getZoneId();
            ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
            instanceFilter.setEndTimeAfter(endTimeAfter.atZone(zone).toOffsetDateTime());
        } else {
            instanceFilter.setEndTimeAfter(null);
        }

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, endTimeAfter != null
                || endTimeBefore != null);
    }

    protected Component createEndTimeBeforeFilter() {
        endTimeBeforeField = uiComponents.create(TypedDateTimePicker.class);
        endTimeBeforeField.setMax(LocalDateTime.now());
        endTimeBeforeField.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        endTimeBeforeField.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        endTimeBeforeField.setLabel(messages.getMessage(ProcessInstanceFilter.class, "ProcessInstanceFilter.endTimeBefore"));

        return endTimeBeforeField;
    }

    protected Component createEndTimeAfterFilter() {
        endTimeAfterField = uiComponents.create(TypedDateTimePicker.class);
        endTimeAfterField.setMax(LocalDateTime.now());
        endTimeAfterField.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        endTimeAfterField.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        endTimeAfterField.setLabel(messages.getMessage(ProcessInstanceFilter.class, "ProcessInstanceFilter.endTimeAfter"));
        setDefaultTime(endTimeAfterField);

        return endTimeAfterField;
    }

    private void setDefaultTime(TypedDateTimePicker<LocalDateTime> dateTimePicker) {
        dateTimePicker.getElement().executeJs(SET_DEFAULT_TIME_SCRIPT);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        LocalDateTime endBeforeValue = getLocalDateTimeParam(queryParameters, END_BEFORE_FILTER_PARAM);
        endTimeBeforeField.setTypedValue(endBeforeValue);

        LocalDateTime endAfterValue = getLocalDateTimeParam(queryParameters, END_AFTER_FILTER_PARAM);
        endTimeAfterField.setTypedValue(endAfterValue);

        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();

        paramValues.put(END_BEFORE_FILTER_PARAM, convertLocalDateTimeParamValue(endTimeBeforeField.getValue()));
        paramValues.put(END_AFTER_FILTER_PARAM, convertLocalDateTimeParamValue(endTimeAfterField.getValue()));

        return paramValues;
    }
}
