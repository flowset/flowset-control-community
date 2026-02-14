/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static io.flowset.control.view.util.JsUtils.SET_DEFAULT_TIME_SCRIPT;

public class EvaluationTimeHeaderFilter
        extends ContainerDataGridHeaderFilter<DecisionInstanceFilter, HistoricDecisionInstanceShortData> {

    protected TypedDateTimePicker<LocalDateTime> evaluatedAfter;
    protected TypedDateTimePicker<LocalDateTime> evaluatedBefore;
    
    protected final Runnable loadDelegate;

    public EvaluationTimeHeaderFilter(DataGrid<HistoricDecisionInstanceShortData> dataGrid,
                                      DataGridColumn<HistoricDecisionInstanceShortData> column,
                                      InstanceContainer<DecisionInstanceFilter> filterDc, Runnable loadDelegate) {
        super(dataGrid, column, filterDc);
        this.loadDelegate = loadDelegate;
    }

    @Override
    public void apply() {
        LocalDateTime startTimeBefore = this.evaluatedBefore.getValue();
        DecisionInstanceFilter decisionInstanceFilter = filterDc.getItem();
        
        if (startTimeBefore != null) {
            ZoneId zoneId = this.evaluatedBefore.getZoneId();
            ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
            decisionInstanceFilter.setEvaluatedBefore(startTimeBefore.atZone(zone).toOffsetDateTime());
        } else {
            decisionInstanceFilter.setEvaluatedBefore(null);
        }
        LocalDateTime startTimeAfter = this.evaluatedAfter.getValue();
        if (startTimeAfter != null) {
            ZoneId zoneId = this.evaluatedAfter.getZoneId();
            ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
            decisionInstanceFilter.setEvaluatedAfter(startTimeAfter.atZone(zone).toOffsetDateTime());
        } else {
            decisionInstanceFilter.setEvaluatedAfter(null);
        }
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, startTimeAfter != null
                || startTimeBefore != null);
        
        this.loadDelegate.run();
    }

    @Override
    protected Component createFilterComponent() {
        Component startTimeAfterFilter = createEvaluatedAfterFilter();
        Component startTimeBeforeFilter = createEvaluatedBeforeFilter();
        VerticalLayout rootLayout = uiComponents.create(VerticalLayout.class);
        rootLayout.setPadding(false);
        rootLayout.setSpacing(false);
        rootLayout.add(startTimeAfterFilter, startTimeBeforeFilter);
        return rootLayout;
    }

    @Override
    protected void resetFilterValues() {
        evaluatedAfter.clear();
        evaluatedBefore.clear();
    }

    @SuppressWarnings("unchecked")
    protected Component createEvaluatedBeforeFilter() {
        evaluatedBefore = uiComponents.create(TypedDateTimePicker.class);
        evaluatedBefore.setMax(LocalDateTime.now());
        evaluatedBefore.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        evaluatedBefore.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        evaluatedBefore.setLabel(messages.getMessage(getClass(), "evaluatedBefore.label"));
        setDefaultTime(evaluatedBefore);
        return evaluatedBefore;
    }

    @SuppressWarnings("unchecked")
    protected Component createEvaluatedAfterFilter() {
        evaluatedAfter = uiComponents.create(TypedDateTimePicker.class);
        evaluatedAfter.setMax(LocalDateTime.now());
        evaluatedAfter.setDatePlaceholder(messages.getMessage(getClass(), "selectDate"));
        evaluatedAfter.setTimePlaceholder(messages.getMessage(getClass(), "selectTime"));
        evaluatedAfter.setLabel(messages.getMessage(getClass(), "evaluatedAfter.label"));
        setDefaultTime(evaluatedAfter);
        return evaluatedAfter;
    }

    protected void setDefaultTime(TypedDateTimePicker<LocalDateTime> dateTimePicker) {
        dateTimePicker.getElement().executeJs(SET_DEFAULT_TIME_SCRIPT);
    }
}
