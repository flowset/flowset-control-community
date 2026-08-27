/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.component.SimplePagination;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Incidents tab fragment inside the History tab of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.history.HistoryIncidentsTabFragment}
 */
@Getter
public class HistoryIncidentsTabFragment extends Composite<HistoryIncidentsTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int INCIDENT_ID_COLUMN_INDEX = 0;
    public static final int ACTIVITY_ID_COLUMN_INDEX = 1;
    public static final int MESSAGE_COLUMN_INDEX = 2;
    public static final int CREATE_TIME_COLUMN_INDEX = 3;
    public static final int END_TIME_COLUMN_INDEX = 4;
    public static final int RESOLVED_COLUMN_INDEX = 5;
    public static final int TYPE_COLUMN_INDEX = 6;

    @TestComponent(path = "incidentsGrid")
    private DataGrid incidentsGrid;

    @TestComponent(path = "incidentsPagination")
    private SimplePagination pagination;

    /**
     * Finds the row in the grid for an historic incident related to the specified activity identifier.
     *
     * @param activityId an activity identifier from BPMN XML
     * @return a grid row or exception thrown if not found
     */
    public DataGrid.Row getRowByActivityId(String activityId) {
        return getRowByCellContent(incidentsGrid, ACTIVITY_ID_COLUMN_INDEX, activityId);
    }

    /**
     * Finds the row in the grid for an historic incident related to the specified identifier.
     *
     * @param incidentId a historic incident identifier
     * @return a grid row or exception thrown if not found
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
     * Selects the row in the grid by the specified activity identifier.
     *
     * @param activityId an activity identifier
     */
    public void selectRowByActivityId(String activityId) {
        getRowByActivityId(activityId)
                .getCellByIndex(CREATE_TIME_COLUMN_INDEX) // use a cell with non-clickable content for clicking row
                .getCellContent()
                .shouldBe(VISIBLE)
                .click();
    }

    /**
     * Opens the context menu for the incidents grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openIncidentsGridContextMenu() {
        return openGridContextMenu(incidentsGrid);
    }
}
