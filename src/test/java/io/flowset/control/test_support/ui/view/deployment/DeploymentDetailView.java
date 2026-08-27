/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.deployment;

import io.flowset.control.test_support.ui.TagNames;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.*;
import io.jmix.masquerade.sys.View;
import lombok.Getter;

import static com.codeborne.selenide.Condition.*;
import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.jmix.masquerade.JConditions.SELECTED;
import static io.jmix.masquerade.JConditions.VISIBLE;

/**
 * Wrapper for the Deployment detail view.
 * Source view: {@link io.flowset.control.view.deploymentdata.DeploymentDetailView}
 */
@Getter
@TestView(id = "bpm_Deployment.detail")
public class DeploymentDetailView extends View<DeploymentDetailView> {

    public static final int RESOURCE_NAME_COLUMN_INDEX = 0;

    @TestComponent(path = "deploymentIdTextField")
    private TextField deploymentIdField;

    @TestComponent(path = "nameTextField")
    private TextField nameField;

    @TestComponent(path = "copyDeploymentId")
    private Button copyDeploymentIdBtn;

    @TestComponent(path = "copyName")
    private Button copyNameBtn;

    @TestComponent(path = "resourcesDataGrid")
    private DataGrid resourcesDataGrid;

    @TestComponent(path = "resourceTabSheet")
    private TabSheet resourceTabSheet;

    @TestComponent(path = "contentCodeEditor")
    private CodeEditor contentCodeEditor;

    @TestComponent(path = "downloadResourceButton")
    private Button downloadResourceButton;

    @TestComponent(path = "emptyResourceMessageContainer")
    private Unknown emptyResourceMessageContainer;

    @TestComponent(path = "closeButton")
    private Button closeButton;

    /**
     * Selects the row  with the given resource name in the data grid.
     *
     * @param resourceName resource name
     */
    public void selectResourceRow(String resourceName) {
        DataGrid.Row resourceRow = getRowByCellContent(resourcesDataGrid, RESOURCE_NAME_COLUMN_INDEX,
                cell -> cell.getCellContent().getText().contains(resourceName));
        resourceRow.getCellByIndex(RESOURCE_NAME_COLUMN_INDEX)
                .getCellContent()
                .click();
    }

    /**
     * Selects the "Running instances" tab in the tab sheet for the selected resource.
     *
     * @return current view
     */
    public DeploymentDetailView selectRunningInstancesTab() {
        resourceTabSheet.getDelegate()
                .$$(TagNames.VAADIN_TAB)
                .findBy(text("Running instances"))
                .click();
        return this;
    }

    /**
     * Selects the "Content" tab in the tab sheet for the selected resource.
     *
     * @return current view
     */
    public DeploymentDetailView selectContentTab() {
        resourceTabSheet.getTabById("contentTab")
                .select()
                .shouldBe(VISIBLE)
                .shouldBe(SELECTED);
        return this;
    }

    /**
     * Opens the process detail view for the process with the specified name from the running instances grid.
     *
     * @param processDefinitionKey process definition key
     * @return current view
     */
    public DeploymentDetailView openProcessFromRunningInstancesGrid(String processDefinitionKey) {
        resourceTabSheet.getDelegate()
                .$$(TagNames.VAADIN_BUTTON)
                .findBy(exactText(processDefinitionKey))
                .shouldBe(visible)
                .click();
        return this;
    }
}
