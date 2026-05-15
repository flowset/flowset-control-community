/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.decisiondefinition;

import io.flowset.control.test_support.ui.UiTestSupport;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionDefinitionDetailView;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the Decision list view.
 * Source view: {@link io.flowset.control.view.decisiondefinition.DecisionDefinitionListView}
 */
@Getter
@TestView(id = "bpm_DecisionDefinition.list")
public class DecisionDefinitionListView extends View<DecisionDefinitionListView> {

    public static final By NAME_BUTTON_BY = byPath("root", "nameBtn");
    public static final By KEY_BUTTON_BY = byPath("root", "keyBtn");
    public static final int NAME_COLUMN_INDEX = 0;
    public static final int KEY_COLUMN_INDEX = 1;
    public static final int VERSION_COLUMN_INDEX = 2;

    @TestComponent(path = "refreshBtn")
    private Button refreshBtn;

    @TestComponent(path = "deployBtn")
    private Button deployBtn;

    @TestComponent(path = "decisionDefinitionsGrid")
    private DataGrid decisionDefinitionsGrid;

    /**
     * Waits until the data is loaded in the data grid.
     *
     * @return current view
     */
    public DecisionDefinitionListView waitUntilDataLoading() {
        UiTestSupport.waitUntilDataLoading(decisionDefinitionsGrid);
        return this;
    }

    /**
     * Finds the row in the grid related to the decision with the specified key.
     *
     * @param decisionKey decision key displayed in the Key column
     * @return found row in data grid
     */
    public DataGrid.Row getRowByDecisionKey(String decisionKey) {
        return getRowByCellContent(decisionDefinitionsGrid, KEY_COLUMN_INDEX, decisionKey);
    }

    /**
     * Opens the detail view for the decision with the specified key.
     *
     * @param decisionKey decision key displayed in the Key column
     * @return detail view for the decision
     */
    public DecisionDefinitionDetailView openDetailViewByKey(String decisionKey) {
        getRowByDecisionKey(decisionKey)
                .getDelegate()
                .doubleClick();

        return $j(DecisionDefinitionDetailView.class)
                .exists();
    }

    /**
     * Opens the context menu for the decision grid.
     *
     * @return grid context menu
     */
    public GridContextMenu openDecisionGridContextActions() {
        UiTestSupport.waitUntilDataLoading(decisionDefinitionsGrid);

        return openGridContextMenu(decisionDefinitionsGrid);
    }
}
