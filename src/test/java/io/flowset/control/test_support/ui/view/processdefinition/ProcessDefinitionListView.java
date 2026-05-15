/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.processdefinition;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.flowset.control.test_support.ui.UiTestSupport;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.component.SimplePagination;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.*;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.By;

import java.util.Map;

import static com.codeborne.selenide.Selectors.byCssSelector;
import static io.flowset.control.test_support.ui.UiTestSupport.*;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for Process list view.
 * Source view: {@link io.flowset.control.view.processdefinition.ProcessDefinitionListView}
 */
@Getter
@TestView(id = "bpm_ProcessDefinition.list")
public class ProcessDefinitionListView extends View<ProcessDefinitionListView> {

    public static final By NAME_BUTTON_BY = byPath("root", "nameBtn");
    public static final By KEY_BUTTON_BY = byPath("root", "keyBtn");
    public static final By PREVIEW_BUTTON_BY = byPath("root", "previewBtn");
    public static final By START_PROCESS_BUTTON_BY = byPath("startProcessBtn");
    public static final By ACTIVATE_BUTTON_BY = byPath("activateBtn");

    public static final By OTHER_PROCESS_ACTIONS_BY = byCssSelector("[jmix-role='jmix-dropdown-button']");
    public static final int NAME_COLUMN_INDEX = 1;
    public static final int KEY_COLUMN_INDEX = 2;
    public static final int VERSION_COLUMN_INDEX = 3;
    public static final int STATE_COLUMN_INDEX = 4;
    public static final int ACTIONS_COLUMN_INDEX = 5;

    @TestComponent(path = "refreshBtn")
    private Button refreshBtn;

    @TestComponent(path = "deployBtn")
    private Button deployBtn;

    @TestComponent(path = "bulkRemoveBtn")
    private Button bulkRemoveBtn;

    @TestComponent(path = "bulkActivateBtn")
    private Button bulkActivateBtn;

    @TestComponent(path = "bulkSuspendBtn")
    private Button bulkSuspendBtn;

    @TestComponent(path = "processDefinitionsGrid")
    private DataGrid processDefinitionsGrid;

    @TestComponent(path = "lastVersionOnlyCb")
    private Checkbox latestVersionCheckbox;

    @TestComponent(path = "nameField")
    private TextField nameField;

    @TestComponent(path = "keyField")
    private TextField keyField;

    @TestComponent(path = "stateComboBox")
    private ComboBox stateComboBox;

    @TestComponent(path = "applyFilterBtn")
    private Button applyFilterBtn;

    @TestComponent(path = "clearBtn")
    private Button clearBtn;

    @TestComponent(path = "processDefinitionPagination")
    private SimplePagination pagination;

    /**
     * Waits until the "Loading..." text disappears from the process grid.
     */
    public ProcessDefinitionListView waitUntilDataLoading() {
        UiTestSupport.waitUntilDataLoading(processDefinitionsGrid);
        return this;
    }

    /**
     * Opens the detail view for the process with the specified key.
     *
     * @param processKey process key
     * @return an opened detail view
     */
    public ProcessDefinitionDetailView openDetailViewByKey(String processKey) {
        getRowByCellContent(processDefinitionsGrid, KEY_COLUMN_INDEX, processKey)
                .getDelegate()
                .doubleClick();

        return $j(ProcessDefinitionDetailView.class).exists()
                .displayed();
    }

    /**
     * Opens the detail view for the specified process. The process row is matched by key and version.
     *
     * @param processKey     process key
     * @param processVersion process version
     * @return an opened detail view
     */
    public ProcessDefinitionDetailView openDetailViewByProcess(String processKey, String processVersion) {
        DataGrid.Row processRow = getRowByProcess(processKey, processVersion);

        processRow.getDelegate()
                .doubleClick();

        return $j(ProcessDefinitionDetailView.class)
                .exists();
    }

    /**
     * Opens the context menu for the process grid.
     *
     * @return opened context menu items
     */
    public GridContextMenu openProcessGridContextActions() {
        return openGridContextMenu(processDefinitionsGrid);
    }

    /**
     * Opens the specified page of the process grid.
     *
     * @param pageNumber page number in data grid to open
     */
    public void openProcessDataGridPage(int pageNumber) {
        for (int i = 0; i < pageNumber; i++) {
            pagination.getNextButton().click();

            waitUntilDataLoading();
        }
    }

    /**
     * Opens additional actions dropdown for the specified row.
     *
     * @param row process row
     * @return dropdown items with actions
     */
    public ElementsCollection openOtherActions(DataGrid.Row row) {
        SelenideElement dropdown = row
                .getCellByIndex(ACTIONS_COLUMN_INDEX)
                .getCellContent()
                .find(OTHER_PROCESS_ACTIONS_BY)
                .shouldBe(VISIBLE);

        return getVisibleDropdownItems(dropdown);
    }

    /**
     * Find the row in the process grid for related to the process with the specified key.
     *
     * @param processKey process key
     * @return found row or throw exception if row not found
     */
    public DataGrid.Row getRowByProcessKey(String processKey) {
        return getRowByCellContent(processDefinitionsGrid, KEY_COLUMN_INDEX, processKey);
    }

    /**
     * Find the row in the process grid related to the process with the specified key and version.
     *
     * @param processKey a process key
     * @param version    a process version
     * @return found row or throw exception if row not found
     */
    public DataGrid.Row getRowByProcess(String processKey, String version) {
        waitUntilDataLoading();

        latestVersionCheckbox.exists()
                .displayed()
                .setChecked(false);

        return getRowByCellContents(processDefinitionsGrid, Map.of(KEY_COLUMN_INDEX, processKey,
                VERSION_COLUMN_INDEX, version));
    }

}
