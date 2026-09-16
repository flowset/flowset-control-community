/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance.detail.tab;

import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.component.TabSheet;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.jmix.masquerade.JConditions.VISIBLE;

/**
 * Wrapper for the Runtime tab fragment of the Process instance detail view.
 * Source component: {@link io.flowset.control.view.processinstance.runtime.RuntimeTabFragment}
 */
@Getter
public class RuntimeTabFragment extends Composite<RuntimeTabFragment> {

    public static final int ACTIVITY_ID_COLUMN_INDEX = 0;

    /**
     * Activity name element inside the hierarchy column. Clicking the tree toggle only expands or collapses
     * the node, so a row is selected by clicking this element.
     *
     * @see io.flowset.control.uicomponent.treedatagrid.NoClickTreeGrid
     */
    public static final By ACTIVITY_NAME_BY = By.cssSelector(".no-click-tree-item-name");

    @TestComponent(path = "runtimeTabFragmentActivityInstancesTree")
    private DataGrid activityInstancesTree;

    @TestComponent(path = "runtimeTabFragmentRuntimeTabsheet")
    private TabSheet tabsheet;

    /**
     * Finds the row in the activity tree related to the specified BPMN activity identifier.
     *
     * @param activityId an activity identifier from BPMN XML
     * @return a visible grid row or exception thrown if not found
     */
    public DataGrid.Row getRowByActivityId(String activityId) {
        return getRowByCellContent(activityInstancesTree, ACTIVITY_ID_COLUMN_INDEX, activityId);
    }

    /**
     * Selects the row in the activity tree by the specified BPMN activity identifier.
     *
     * @param activityId an activity identifier from BPMN XML
     */
    public void selectRowByActivityId(String activityId) {
        getRowByActivityId(activityId)
                .getCellByIndex(ACTIVITY_ID_COLUMN_INDEX)
                .getCellContent()
                .find(ACTIVITY_NAME_BY)
                .shouldBe(VISIBLE)
                .click();
    }
}
