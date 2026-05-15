/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.incident;

import io.flowset.control.test_support.ui.UiTestSupport;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the Incident list view.
 * Source view: {@link io.flowset.control.view.incidentdata.IncidentDataListView}
 */
@Getter
@TestView(id = "IncidentData.list")
public class IncidentListView extends View<IncidentListView> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");
    public static final By INLINE_RETRY_BUTTON_BY = byPath("incidentActionsBox", "retryIncidentBtn");

    public static final int CHECKBOX_COLUMN_INDEX = 0;
    public static final int INCIDENT_ID_COLUMN_INDEX = 1;
    public static final int ACTIVITY_ID_COLUMN_INDEX = 2;
    public static final int TIMESTAMP_COLUMN_INDEX = 3;
    public static final int MESSAGE_COLUMN_INDEX = 4;
    public static final int PROCESS_INSTANCE_ID_COLUMN_INDEX = 5;
    public static final int PROCESS_DEFINITION_ID_COLUMN_INDEX = 6;
    public static final int TYPE_COLUMN_INDEX = 7;
    public static final int ACTIONS_COLUMN_INDEX = 8;

    @TestComponent(path = "refreshBtn")
    private Button refreshButton;

    @TestComponent(path = "bulkRetryBtn")
    private Button bulkRetryButton;

    @TestComponent(path = "incidentsDataGrid")
    private DataGrid incidentsGrid;

    /**
     * Waits until the data is loaded in the incident data grid.
     *
     * @return this instance for method chaining
     */
    public IncidentListView waitUntilDataLoading() {
        UiTestSupport.waitUntilDataLoading(incidentsGrid);
        return this;
    }

    /**
     * Opens the context menu for the incident grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openIncidentsGridContextMenu() {
        return openGridContextMenu(incidentsGrid);
    }

    /**
     * Opens the detail view for the incident with the specified id.
     *
     * @param incidentId incident id
     * @return opened detail view
     */
    public IncidentDataDetailView openDetailViewByIncidentId(String incidentId) {
        getRowByIncidentId(incidentId)
                .getCellByIndex(INCIDENT_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .shouldBe(VISIBLE)
                .click();

        return $j(IncidentDataDetailView.class)
                .exists()
                .displayed();
    }

    /**
     * Finds the row in the incident grid by incident id.
     *
     * @param incidentId incident id
     * @return row for the specified incident id
     */
    public DataGrid.Row getRowByIncidentId(String incidentId) {
        return getRowByCellContent(incidentsGrid, INCIDENT_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(incidentId);
        });
    }

    /**
     * Finds the row in the incident grid by activity id.
     *
     * @param activityId activity id
     * @return row for the incident related to the specified activity id
     */
    public DataGrid.Row getRowByActivityId(String activityId) {
        return getRowByCellContent(incidentsGrid, ACTIVITY_ID_COLUMN_INDEX, activityId);
    }

    /**
     * Selects the row in the incident grid related to the specified activity id.
     *
     * @param activityId activity id
     */
    public void selectRowByActivityId(String activityId) {
        getRowByActivityId(activityId)
                .getCellByIndex(ACTIVITY_ID_COLUMN_INDEX)// use a cell with non-clickable content for clicking row
                .getCellContent()
                .click();
    }
}
