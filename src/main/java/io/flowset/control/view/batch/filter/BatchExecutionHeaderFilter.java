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
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.facet.urlqueryparameters.HasFilterUrlParamHeaderFilter;
import io.flowset.control.view.batch.BatchExecutionFilterOption;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.model.InstanceContainer;

import java.util.HashMap;
import java.util.Map;

import static io.flowset.control.facet.urlqueryparameters.ActiveBatchListQueryParamBinder.EXECUTION_PARAM;
import static io.flowset.control.view.util.FilterQueryParamUtils.getSingleParam;

public class BatchExecutionHeaderFilter extends BatchHeaderFilter implements HasFilterUrlParamHeaderFilter {
    protected JmixRadioButtonGroup<BatchExecutionFilterOption> executionGroup;

    public BatchExecutionHeaderFilter(Grid<BatchStatisticsData> dataGrid,
                                      DataGridColumn<BatchStatisticsData> column,
                                      InstanceContainer<BatchFilter> filterDc) {
        super(dataGrid, column, filterDc);
    }

    @Override
    protected Component createFilterComponent() {
        executionGroup = uiComponents.create(JmixRadioButtonGroup.class);
        executionGroup.setLabel(messages.getMessage("io.flowset.control.view.batch/allBatchListView.execution"));
        executionGroup.setItems(BatchExecutionFilterOption.class);

        VerticalLayout layout = uiComponents.create(VerticalLayout.class);
        layout.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.Padding.SMALL);
        layout.setSizeFull();
        layout.add(executionGroup);

        return layout;
    }

    @Override
    public void apply() {
        BatchExecutionFilterOption option = executionGroup.getValue();
        BatchFilter filter = filterDc.getItem();

        if (option == BatchExecutionFilterOption.WITH_FAILURES) {
            filter.setWithFailures(true);
            filter.setWithoutFailures(null);
        } else if (option == BatchExecutionFilterOption.WITHOUT_FAILURES) {
            filter.setWithFailures(null);
            filter.setWithoutFailures(true);
        } else {
            filter.setWithFailures(null);
            filter.setWithoutFailures(null);
        }

        boolean isFilterActive = option != null && option != BatchExecutionFilterOption.ALL;
        filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME, isFilterActive);
    }

    @Override
    public void updateComponents(QueryParameters queryParameters) {
        BatchExecutionFilterOption option = getSingleParam(queryParameters, EXECUTION_PARAM, BatchExecutionFilterOption::fromId);
        if (option == null) {
            option = BatchExecutionFilterOption.ALL;
        }

        executionGroup.setValue(option);
        apply();
    }

    @Override
    public Map<String, String> getQueryParamValues() {
        Map<String, String> paramValues = new HashMap<>();
        BatchExecutionFilterOption option = executionGroup.getValue();

        paramValues.put(EXECUTION_PARAM, option != null ? option.getId() : null);

        return paramValues;
    }

    @Override
    protected void resetFilterValues() {
        executionGroup.setValue(BatchExecutionFilterOption.ALL);
    }
}
