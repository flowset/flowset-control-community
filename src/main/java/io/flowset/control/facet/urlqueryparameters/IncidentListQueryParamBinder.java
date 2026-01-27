/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.facet.urlqueryparameters;

import com.vaadin.flow.router.QueryParameters;
import io.flowset.control.entity.incident.IncidentData;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;

public class IncidentListQueryParamBinder extends GridCustomHeaderFilterUrlQueryParametersBinder<IncidentData> {
    public static final String ACTIVITY_ID_FILTER_PARAM = "activityId";
    public static final String TIMESTAMP_BEFORE_FILTER_PARAM = "timestampBefore";
    public static final String TIMESTAMP_AFTER_FILTER_PARAM = "timestampAfter";
    public static final String MESSAGE_FILTER_PARAM = "incidentMessage";
    public static final String PROCESS_INSTANCE_ID_FILTER_PARAM = "processInstanceId";

    public static final String TYPE_FILTER_PARAM = "type";

    protected final Runnable loadDelegate;

    public IncidentListQueryParamBinder(DataGrid<IncidentData> dataGrid, Runnable loadDelegate) {
        super(dataGrid);
        this.loadDelegate = loadDelegate;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        super.updateState(queryParameters);

        this.loadDelegate.run();
    }

    @Override
    protected void handleFilterApply(DataGridHeaderFilter.ApplyEvent applyEvent) {
        super.handleFilterApply(applyEvent);

        this.loadDelegate.run();
    }
}
