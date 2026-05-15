/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.decisioninstance;

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
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the Decision instance list view.
 * Source view: {@link io.flowset.control.view.decisioninstance.DecisionInstanceDataListView}
 */
@Getter
@TestView(id = "DecisionInstanceData.list")
public class DecisionInstanceListView extends View<DecisionInstanceListView> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");
    public static final By PROCESS_INSTANCE_BUTTON_BY = byPath("root", "processInstanceBtn");
    public static final By PROCESS_ID_BUTTON_BY = byPath("root", "processIdBtn");

    public static final int DECISION_INSTANCE_ID_COLUMN_INDEX = 0;
    public static final int DECISION_DEFINITION_ID_COLUMN_INDEX = 1;
    public static final int EVALUATION_TIME_COLUMN_INDEX = 2;
    public static final int PROCESS_INSTANCE_ID_COLUMN_INDEX = 3;
    public static final int PROCESS_DEFINITION_ID_COLUMN_INDEX = 4;
    public static final int ACTIVITY_ID_COLUMN_INDEX = 5;

    @TestComponent(path = "refreshButton")
    private Button refreshButton;

    @TestComponent(path = "decisionInstancesDataGrid")
    private DataGrid decisionInstancesDataGrid;

    /**
     * Waits until the data is loaded in the data grid.
     *
     * @return current view
     */
    public DecisionInstanceListView waitUntilDataLoading() {
        UiTestSupport.waitUntilDataLoading(decisionInstancesDataGrid);
        return this;
    }

    /**
     * Finds the row in the grid related to the decision instance with the specified id.
     *
     * @param decisionInstanceId decision instance id displayed in the ID column
     * @return found row in data grid
     */
    public DataGrid.Row getRowByDecisionInstanceId(String decisionInstanceId) {
        return getRowByCellContent(decisionInstancesDataGrid, DECISION_INSTANCE_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(decisionInstanceId);
        });
    }

    /**
     * Opens the context menu for the decision instances grid.
     *
     * @return grid context menu
     */
    public GridContextMenu openInstancesGridContextMenu() {
        return openGridContextMenu(decisionInstancesDataGrid);
    }

    /**
     * Opens the detail view for the decision instance with the specified id.
     *
     * @param decisionInstanceId decision instance id displayed in the ID column
     * @return opened detail view
     */
    public DecisionInstanceDetailView openDetailViewByDecisionInstanceId(String decisionInstanceId) {
        getRowByDecisionInstanceId(decisionInstanceId)
                .getCellByIndex(DECISION_INSTANCE_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ID_BUTTON_BY)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .click();

        return $j(DecisionInstanceDetailView.class)
                .exists()
                .displayed();
    }
}
