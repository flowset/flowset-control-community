/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.decisiondefinition.detail;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Decision instances tab fragment inside the Decision detail view.
 * Source component: {@link io.flowset.control.view.decisioninstance.DecisionInstancesFragment}
 */
@Slf4j
@Getter
public class DecisionInstancesTabFragment extends Composite<DecisionInstancesTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");
    public static final By PROCESS_ID_BUTTON_BY = byPath("root", "processIdBtn");
    public static final By PROCESS_INSTANCE_BUTTON_BY = byPath("root", "processInstanceBtn");

    public static final int DECISION_INSTANCE_ID_COLUMN_INDEX = 0;
    public static final int EVALUATION_TIME_COLUMN_INDEX = 1;
    public static final int PROCESS_DEFINITION_ID_COLUMN_INDEX = 2;
    public static final int PROCESS_INSTANCE_ID_COLUMN_INDEX = 3;
    public static final int ACTIVITY_ID_COLUMN_INDEX = 4;

    @TestComponent(path = "decisionInstancesFragmentRefreshDecisionInstanceBtn")
    private Button refreshButton;

    @TestComponent(path = "decisionInstancesFragmentDecisionInstancesGrid")
    private DataGrid decisionInstancesGrid;

    /**
     * Find the first row in the decision instances grid related to the instance with the specified id.
     *
     * @param decisionInstanceId decision instance id displayed in the ID column
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByInstanceId(String decisionInstanceId) {
        return getRowByCellContent(decisionInstancesGrid, DECISION_INSTANCE_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();

            return idBtnText.equals(decisionInstanceId);
        });
    }

    public GridContextMenu openInstancesGridContextMenu() {
        return openGridContextMenu(decisionInstancesGrid);
    }
}
