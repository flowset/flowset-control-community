/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.ActiveBatchListQueryParamBinder.STARTED_AFTER_PARAM;
import static io.flowset.control.facet.urlqueryparameters.ActiveBatchListQueryParamBinder.STARTED_BEFORE_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.convertOffsetDateTimeParamValue;
import static io.flowset.control.view.util.FilterQueryParamUtils.getOffsetDateTimeParam;
import static io.flowset.control.view.util.JsUtils.SET_DEFAULT_TIME_SCRIPT;

public class BatchStartTimeHeaderFilter extends BatchHeaderFilter implements HasFilterUrlParamHeaderFilter {
    protected TypedDateTimePicker<OffsetDateTime> startedAfterField;
    protected TypedDateTimePicker<OffsetDateTime> startedBeforeField;

    public BatchStartTimeHeaderFilter(Grid<BatchStatisticsData> dataGrid,
                                      DataGridColumn<BatchStatisticsData> column,
                                      InstanceContainer<BatchFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    protected Component createFilterComponent() {
        startedAfterField = uiComponents.create(TypedDateTimePicker.class);
        startedAfterField.setWidthFull();
        startedAfterField.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        startedAfterField.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        startedAfterField.setLabel(messages.getMessage(BatchFilter.class, "BatchFilter.startedAfter"));
        setDefaultTime(startedAfterField);

        startedBeforeField = uiComponents.create(TypedDateTimePicker.class);
        startedBeforeField.setWidthFull();
        startedBeforeField.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        startedBeforeField.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        startedBeforeField.setLabel(messages.getMessage(BatchFilter.class, "BatchFilter.startedBefore"));
        setDefaultTime(startedBeforeField);

        VerticalLayout layout = uiComponents.create(VerticalLayout.class);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.add(startedAfterField, startedBeforeField);

        return layout;
    }

    @Override
    public void apply() {
        BatchFilter filter = filterDc.getItem();
        OffsetDateTime startedAfter = toOffsetDateTime(startedAfterField.getValue(), startedAfterField.getZoneId());
        OffsetDateTime startedBefore = toOffsetDateTime(startedBeforeField.getValue(), startedBeforeField.getZoneId());

        filter.setStartedAfter(startedAfter);
        filter.setStartedBefore(startedBefore);

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME,
                startedAfter != null || startedBefore != null);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        OffsetDateTime startedAfter = getOffsetDateTimeParam(queryParameters, STARTED_AFTER_PARAM);
        startedAfterField.setValue(toLocalDateTime(startedAfter));

        OffsetDateTime startedBefore = getOffsetDateTimeParam(queryParameters, STARTED_BEFORE_PARAM);
        startedBeforeField.setValue(toLocalDateTime(startedBefore));

        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put(STARTED_AFTER_PARAM,
                convertOffsetDateTimeParamValue(toOffsetDateTime(startedAfterField.getValue(), startedAfterField.getZoneId())));
        paramValues.put(STARTED_BEFORE_PARAM,
                convertOffsetDateTimeParamValue(toOffsetDateTime(startedBeforeField.getValue(), startedBeforeField.getZoneId())));

        return paramValues;
    }

    @Override
    protected void resetFilterValues() {
        startedAfterField.clear();
        startedBeforeField.clear();
    }

    protected OffsetDateTime toOffsetDateTime(LocalDateTime value, ZoneId zoneId) {
        if (value == null) {
            return null;
        }
        ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();

        return value.atZone(zone).toOffsetDateTime();
    }

    protected LocalDateTime toLocalDateTime(OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }

    protected void setDefaultTime(TypedDateTimePicker<OffsetDateTime> dateTimePicker) {
        dateTimePicker.getElement().executeJs(SET_DEFAULT_TIME_SCRIPT);
    }
}
