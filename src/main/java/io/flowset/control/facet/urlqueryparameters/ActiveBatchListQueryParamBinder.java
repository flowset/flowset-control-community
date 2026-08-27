/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;

import java.util.Map;

import static io.flowset.control.view.util.FilterQueryParamUtils.getStringParam;

public class ActiveBatchListQueryParamBinder extends GridCustomHeaderFilterUrlQueryParametersBinder<BatchStatisticsData> {
    public static final String TAB_PARAM = "tab";
    public static final String ID_PARAM = "id";
    public static final String TYPE_PARAM = "type";
    public static final String STATE_PARAM = "state";
    public static final String EXECUTION_PARAM = "execution";
    public static final String STARTED_AFTER_PARAM = "startedAfter";
    public static final String STARTED_BEFORE_PARAM = "startedBefore";

    protected final JmixTabSheet tabSheet;
    protected final Runnable activeBatchLoadDelegate;

    public ActiveBatchListQueryParamBinder(DataGrid<BatchStatisticsData> dataGrid, JmixTabSheet tabSheet, Runnable activeBatchLoadDelegate) {
        super(dataGrid);
        this.tabSheet = tabSheet;
        this.activeBatchLoadDelegate = activeBatchLoadDelegate;

        initListeners();
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        String tabParameter = getStringParam(queryParameters, TAB_PARAM);
        if (tabParameter != null) {
            tabSheet.setSelectedIndex(Integer.parseInt(tabParameter));
        }

        super.updateState(queryParameters);

        this.activeBatchLoadDelegate.run();
    }

    @Override
    protected void handleFilterApply(DataGridHeaderFilter.ApplyEvent applyEvent) {
        super.handleFilterApply(applyEvent);

        this.activeBatchLoadDelegate.run();
    }

    private void initListeners() {
        tabSheet.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            String tabId = selectedTab != null ? String.valueOf(tabSheet.getIndexOf(selectedTab)) : null;
            if (tabId != null) {
                updateQueryParams(Map.of(TAB_PARAM, tabId));
            } else {
                updateQueryParams(Map.of());
            }
        });
    }
}
