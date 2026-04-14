/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch.filter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.batch.RuntimeBatchData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.view.batch.BatchStateFilterOption;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.model.InstanceContainer;

import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.ActiveBatchListQueryParamBinder.STATE_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getSingleParam;

public class BatchStateHeaderFilter extends BatchHeaderFilter implements HasFilterUrlParamHeaderFilter {
    protected JmixRadioButtonGroup<BatchStateFilterOption> stateGroup;

    public BatchStateHeaderFilter(Grid<BatchStatisticsData> dataGrid,
                                  DataGridColumn<BatchStatisticsData> column,
                                  InstanceContainer<BatchFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    protected Component createFilterComponent() {
        stateGroup = uiComponents.create(JmixRadioButtonGroup.class);
        stateGroup.setLabel(messages.getMessage(RuntimeBatchData.class, "RuntimeBatchData.state"));
        stateGroup.setItems(BatchStateFilterOption.class);

        VerticalLayout layout = uiComponents.create(VerticalLayout.class);
        layout.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.Padding.SMALL);
        layout.setSizeFull();
        layout.setMinWidth("20em");
        layout.add(stateGroup);

        return layout;
    }

    @Override
    public void apply() {
        BatchStateFilterOption option = stateGroup.getValue();
        BatchFilter filter = filterDc.getItem();

        if (option == BatchStateFilterOption.ACTIVE) {
            filter.setSuspended(false);
        } else if (option == BatchStateFilterOption.SUSPENDED) {
            filter.setSuspended(true);
        } else {
            filter.setSuspended(null);
        }

        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, option != null);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        stateGroup.setValue(getSingleParam(queryParameters, STATE_PARAM, BatchStateFilterOption::fromId));

        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        BatchStateFilterOption option = stateGroup.getValue();
        paramValues.put(STATE_PARAM, option != null ? option.getId() : null);

        return paramValues;
    }

    @Override
    protected void resetFilterValues() {
        stateGroup.clear();
    }
}
