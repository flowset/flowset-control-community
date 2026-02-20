/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.jmix.flowui.component.grid.DataGrid;

public class DecisionInstanceListQueryParamBinder extends GridCustomHeaderFilterUrlQueryParametersBinder<HistoricDecisionInstanceShortData> {
    public static final String ID_FILTER_PARAM = "id";
    public static final String DECISION_KEY_FILTER_PARAM = "decisionKey";
    public static final String DECISION_ID_FILTER_PARAM = "decisionId";
    public static final String PROCESS_INSTANCE_ID_FILTER_PARAM = "processInstanceId";
    public static final String ACTIVITY_ID_FILTER_PARAM = "activityId";

    public static final String EVALUATED_AFTER_FILTER_PARAM = "evaluatedAfter";
    public static final String EVALUATED_BEFORE_FILTER_PARAM = "evaluatedBefore";

    protected final Runnable loadDelegate;

    public DecisionInstanceListQueryParamBinder(DataGrid<HistoricDecisionInstanceShortData> dataGrid, Runnable loadDelegate) {
        super(dataGrid);
        this.loadDelegate = loadDelegate;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        super.updateState(queryParameters);

        this.loadDelegate.run();
    }
}
