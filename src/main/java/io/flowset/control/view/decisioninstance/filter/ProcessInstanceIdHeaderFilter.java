/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.decisioninstance.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.model.InstanceContainer;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.uicomponent.ContainerDataGridHeaderFilter;

public class ProcessInstanceIdHeaderFilter
        extends ContainerDataGridHeaderFilter<DecisionInstanceFilter, HistoricDecisionInstanceShortData> {

    protected TextField processInstanceId;

    protected final Runnable loadDelegate;

    public ProcessInstanceIdHeaderFilter(DataGrid<HistoricDecisionInstanceShortData> dataGrid,
                                         DataGridColumn<HistoricDecisionInstanceShortData> column,
                                         InstanceContainer<DecisionInstanceFilter> filterDc, Runnable loadDelegate) {
        super(dataGrid, column, filterDc);
        this.loadDelegate = loadDelegate;
    }

    @Override
    public void apply() {
        String value = processInstanceId.getValue();
        filterDc.getItem().setProcessInstanceId(value);
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, value != null);

        this.loadDelegate.run();
    }

    @Override
    protected Component createFilterComponent() {
        return createProcessInstanceIdFilter();
    }

    @Override
    protected void resetFilterValues() {
        processInstanceId.clear();
    }

    protected TextField createProcessInstanceIdFilter() {
        processInstanceId = uiComponents.create(TextField.class);
        processInstanceId.setWidthFull();
        processInstanceId.setMinWidth("30em");
        processInstanceId.setClearButtonVisible(true);
        processInstanceId.setLabel(messages.getMessage(DecisionInstanceFilter.class,
                "DecisionInstanceFilter.processInstanceId"));
        processInstanceId.setPlaceholder(messages.getMessage(getClass(), "processInstanceId.placeHolder"));
        return processInstanceId;
    }
}
