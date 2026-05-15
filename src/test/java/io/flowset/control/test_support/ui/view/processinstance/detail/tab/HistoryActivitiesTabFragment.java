/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.flowset.control.test_support.ui.component.GridContextMenu;
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
 * Wrapper for the Activities tab fragment inside the History tab of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.history.ActivitiesTabFragment}
 */
@Getter
public class HistoryActivitiesTabFragment extends Composite<HistoryActivitiesTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int ACTIVITY_INSTANCE_ID_COLUMN_INDEX = 0;
    public static final int ACTIVITY_ID_COLUMN_INDEX = 1;
    public static final int ACTIVITY_NAME_COLUMN_INDEX = 2;
    public static final int START_TIME_COLUMN_INDEX = 3;
    public static final int END_TIME_COLUMN_INDEX = 4;
    public static final int ASSIGNEE_COLUMN_INDEX = 5;

    @TestComponent(path = "historicActivityInstancesGrid")
    private DataGrid historicActivityInstancesGrid;

    /**
     * Finds the row in the grid for an activity related to the specified activity identifier.
     *
     * @param activityId an activity identifier from BPMN XML
     * @return a grid row or exception thrown if not found
     */
    public DataGrid.Row getRowByActivityId(String activityId) {
        return getRowByCellContent(historicActivityInstancesGrid, ACTIVITY_ID_COLUMN_INDEX, activityId);
    }

    /**
     * Finds the row in the grid for an activity related to the specified identifier.
     *
     * @param activityInstanceId an activity instance identifier
     * @return a grid row or exception thrown if not found
     */
    public DataGrid.Row getRowByActivityInstanceId(String activityInstanceId) {
        return getRowByCellContent(historicActivityInstancesGrid, ACTIVITY_INSTANCE_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(activityInstanceId);
        });
    }

    /**
     * Opens the context menu for the activity grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openActivityGridContextMenu() {
        return openGridContextMenu(historicActivityInstancesGrid);
    }

    /**
     * Selects the row in the grid by the specified activity identifier.
     *
     * @param activityId an activity identifier
     */
    public void selectRowByActivityId(String activityId) {
        getRowByActivityId(activityId)
                .getCellByIndex(ACTIVITY_ID_COLUMN_INDEX)// use a cell with non-clickable content for clicking row
                .getCellContent()
                .shouldBe(VISIBLE)
                .click();
    }
}
