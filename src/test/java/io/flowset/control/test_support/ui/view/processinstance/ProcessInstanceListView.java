/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processinstance;

import io.flowset.control.test_support.ui.UiTestSupport;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.processinstance.detail.ProcessInstanceDetailView;
import io.flowset.control.view.processinstance.ProcessInstanceViewMode;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the Process instance list view.
 * Source view: {@link io.flowset.control.view.processinstance.ProcessInstanceListView}
 */
@Getter
@TestView(id = "bpm_ProcessInstance.list")
public class ProcessInstanceListView extends View<ProcessInstanceListView> {

    public static final By ID_BUTTON_BY = byPath("root", "idBtn");
    public static final By PROCESS_BUTTON_BY = byPath("root", "idBtn");
    public static final By VIEW_TERMINATE_REASON_BUTTON_BY = byPath("root", "viewTerminateReasonBtn");

    public static final int ID_COLUMN_INDEX = 1;
    public static final int PROCESS_COLUMN_INDEX = 2;
    public static final int BUSINESS_KEY_COLUMN_INDEX = 3;
    public static final int STATE_COLUMN_INDEX = 4;

    @TestComponent(path = "refreshBtn")
    private Button refreshBtn;

    @TestComponent(path = "bulkTerminateBtn")
    private Button bulkTerminateBtn;

    @TestComponent(path = "bulkSuspendBtn")
    private Button bulkSuspendBtn;

    @TestComponent(path = "bulkActivateBtn")
    private Button bulkActivateBtn;

    @TestComponent(path = "processInstancesGrid")
    private DataGrid processInstancesGrid;

    /**
     * Waits until the data is loaded in the process instances grid.
     */
    public void waitUntilDataLoading() {
        UiTestSupport.waitUntilDataLoading(processInstancesGrid);
    }

    /**
     * Opens the context menu for the process instances grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openInstancesGridContextActions() {
        return openGridContextMenu(processInstancesGrid);
    }

    /**
     * Find the row in the process instances grid related to the instance with the specified id.
     *
     * @param instanceId process instance id displayed in the ID column
     * @return found row or throws exception if row not found
     */
    public DataGrid.Row getRowByInstanceId(String instanceId) {
        waitUntilDataLoading();

        return getRowByCellContent(processInstancesGrid, ID_COLUMN_INDEX, cell -> {
            String idBtnText = cell.getCellContent()
                    .find(ID_BUTTON_BY)
                    .getText();
            return idBtnText.equals(instanceId);
        });
    }

    /**
     * Switch the view-mode toggle to ALL so that finished instances are also displayed.
     * The toggle buttons inside #modeButtonsGroup do not have ids; ALL is the first button.
     */
    public ProcessInstanceListView switchToAllViewMode() {
        switchToMode(ProcessInstanceViewMode.ALL);
        return this;
    }

    /**
     * Switch the view-mode toggle to the specified mode (Active, Completed, etc.)
     *
     * @param mode view mode to switch to
     * @return opened view mode
     */
    public ProcessInstanceListView switchToMode(ProcessInstanceViewMode mode) {
        $("#modeButtonsGroup")
                .$$("vaadin-button")
                .get(mode.ordinal())
                .shouldBe(VISIBLE).click();
        waitUntilDataLoading();

        return this;
    }

    /**
     * Opens the detail view of the process instance with the specified id.
     *
     * @param instanceId process instance id displayed in the ID column
     * @return process instance detail view
     */
    public ProcessInstanceDetailView openDetailViewByInstanceId(String instanceId) {
        getRowByInstanceId(instanceId)
                .getCellByIndex(BUSINESS_KEY_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        return $j(ProcessInstanceDetailView.class).exists()
                .displayed();
    }
}
