/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Incidents tab fragment inside the Runtime tab of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.runtime.RuntimeIncidentsTabFragment}
 */
@Getter
public class RuntimeIncidentsTabFragment extends Composite<RuntimeIncidentsTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int INCIDENT_ID_COLUMN_INDEX = 0;
    public static final int ACTIVITY_ID_COLUMN_INDEX = 1;
    public static final int MESSAGE_COLUMN_INDEX = 2;
    public static final int TIMESTAMP_COLUMN_INDEX = 3;

    @TestComponent(path = "runtimeIncidentRetryBtn")
    private Button retryButton;

    @TestComponent(path = "runtimeIncidentsGrid")
    private DataGrid runtimeIncidentsGrid;

    /**
     * Finds the row in the grid for an incident related to the specified activity.
     *
     * @param activityId an activity identifier from BPMN XML
     * @return a grid row or exception thrown if not found
     */
    public DataGrid.Row getRowByActivityId(String activityId) {
        return getRowByCellContent(runtimeIncidentsGrid, ACTIVITY_ID_COLUMN_INDEX, activityId);
    }

    /**
     * Finds the row in the grid for an incident related the specified identifier.
     *
     * @param incidentId an incident identifier
     * @return a grid row or exception thrown if not found
     */
    public DataGrid.Row getRowByIncidentId(String incidentId) {
        return getRowByCellContent(runtimeIncidentsGrid, INCIDENT_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(incidentId);
        });
    }

    /**
     * Selects the row in the grid by the specified incident identifier.
     *
     * @param incidentId an incident identifier
     */
    public void selectRowByIncidentId(String incidentId) {
        getRowByIncidentId(incidentId)
                .getCellByIndex(MESSAGE_COLUMN_INDEX) // first column is ID column opens a view, use non-clickable cell for click
                .getCellContent()
                .shouldBe(VISIBLE)
                .click();
    }

    /**
     * Selects the row in the grid for an incident related to the specified activity.
     *
     * @param activityId an activity identifier from BPMN XML
     */
    public void selectRowByActivityId(String activityId) {
        getRowByActivityId(activityId)
                .getCellByIndex(ACTIVITY_ID_COLUMN_INDEX)
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
        return openGridContextMenu(runtimeIncidentsGrid);
    }
}
