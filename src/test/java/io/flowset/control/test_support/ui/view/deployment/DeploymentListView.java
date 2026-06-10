/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.deployment;

import io.flowset.control.test_support.ui.UiTestSupport;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.component.SimplePagination;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.component.DateTimePicker;
import io.jmix.masquerade.component.TextField;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JSelectors.byPath;

/**
 * Wrapper for the Deployment list view.
 * Source view: {@link io.flowset.control.view.deploymentdata.DeploymentListView}
 */
@Getter
@TestView(id = "bpm_Deployment.list")
public class DeploymentListView extends View<DeploymentListView> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");

    public static final int CHECKBOX_COLUMN_INDEX = 0;
    public static final int DEPLOYMENT_ID_COLUMN_INDEX = 1;
    public static final int NAME_COLUMN_INDEX = 2;
    public static final int DEPLOYMENT_TIME_COLUMN_INDEX = 3;
    public static final int SOURCE_COLUMN_INDEX = 4;
    public static final int TENANT_ID_COLUMN_INDEX = 5;

    @TestComponent(path = "nameField")
    private TextField nameField;

    @TestComponent(path = "deploymentAfterField")
    private DateTimePicker deploymentAfterField;

    @TestComponent(path = "deploymentBeforeField")
    private DateTimePicker deploymentBeforeField;

    @TestComponent(path = "applyFilterBtn")
    private Button applyFilterBtn;

    @TestComponent(path = "clearBtn")
    private Button clearBtn;

    @TestComponent(path = "refreshBtn")
    private Button refreshBtn;

    @TestComponent(path = "bulkRemoveBtn")
    private Button bulkRemoveBtn;

    @TestComponent(path = "pagination")
    private SimplePagination pagination;

    @TestComponent(path = "deploymentsDataGrid")
    private DataGrid deploymentsDataGrid;

    /**
     * Waits until the data is loaded in the deployment data grid.
     *
     * @return current view
     */
    public DeploymentListView waitUntilDataLoading() {
        UiTestSupport.waitUntilDataLoading(deploymentsDataGrid);
        return this;
    }

    /**
     * Opens the context menu for the deployment data grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openDeploymentsGridContextMenu() {
        return openGridContextMenu(deploymentsDataGrid);
    }

    /**
     * Find the row in the deployment data grid related to the deployment with the specified name.
     *
     * @param name a deployment name
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByName(String name) {
        return getRowByCellContent(deploymentsDataGrid, NAME_COLUMN_INDEX, name);
    }

    /**
     * Find the first row in the deployment data grid related to the deployment with the specified id.
     *
     * @param deploymentId a deployment id (text rendered by the {@code idBtn} link inside the Deployment ID column)
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByDeploymentId(String deploymentId) {
        return getRowByCellContent(deploymentsDataGrid, DEPLOYMENT_ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(deploymentId);
        });
    }
}
