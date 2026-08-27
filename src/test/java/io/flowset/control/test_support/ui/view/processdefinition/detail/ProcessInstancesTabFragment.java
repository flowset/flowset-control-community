/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition.detail;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.Composite;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Process instances tab fragment inside the Process detail view.
 * Source component: {@link io.flowset.control.view.processdefinition.ProcessInstancesFragment}
 */
@Getter
public class ProcessInstancesTabFragment extends Composite<ProcessInstancesTabFragment> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");
    public static final int ID_COLUMN_INDEX = 1;
    public static final int BUSINESS_KEY_COLUMN_INDEX = 2;
    public static final int STATE_COLUMN_INDEX = 3;

    @TestComponent(path = "processInstancesFragmentRefreshProcessInstanceBtn")
    private Button refreshButton;

    @TestComponent(path = "processInstancesFragmentBulkTerminateBtn")
    private Button terminateButton;

    @TestComponent(path = "processInstancesFragmentBulkSuspendBtn")
    private Button suspendButton;

    @TestComponent(path = "processInstancesFragmentBulkActivateBtn")
    private Button activateButton;

    @TestComponent(path = "processInstancesFragmentProcessInstancesGrid")
    private DataGrid processInstancesGrid;

    /**
     * Find the first row in the process instances grid related to the instance with the specified id.
     *
     * @param instanceId process instance id displayed in the ID column
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByInstanceId(String instanceId) {
        return getRowByCellContent(processInstancesGrid, ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(instanceId);
        });
    }

    /**
     * Opens the context menu for the process instances grid.
     *
     * @return grid context menu
     */
    public GridContextMenu openInstancesGridContextMenu() {
        return openGridContextMenu(processInstancesGrid);
    }
}
