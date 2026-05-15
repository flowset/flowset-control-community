/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.engine;

import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.apache.commons.lang3.Strings;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.getRowByCellContent;
import static io.flowset.control.test_support.ui.UiTestSupport.openGridContextMenu;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the BPM engine list view.
 * Source view: {@link io.flowset.control.view.bpmengine.BpmEngineListView}
 */
@Getter
@TestView(id = "BpmEngine.list")
public class BpmEngineListView extends View<BpmEngineListView> {
    public static final By NAME_BUTTON_BY = byPath("root", "nameBtn");
    public static final By ITEM_ACTIONS_BY = byPath("bpmEngineActionsBox");
    public static final By MARK_AS_DEFAULT_BUTTON_BY = byPath("bpmEngineActionsBox", "markAsDefaultBtn");

    public static final int NAME_COLUMN_INDEX = 1;
    public static final int BASE_URL_COLUMN_INDEX = 2;
    public static final int TYPE_COLUMN_INDEX = 3;
    public static final int ACTIONS_COLUMN_INDEX = 4;

    @TestComponent(path = "refreshButton")
    private Button refreshButton;

    @TestComponent(path = "createButton")
    private Button createButton;

    @TestComponent(path = "removeButton")
    private Button removeButton;

    @TestComponent(path = "bpmEnginesDataGrid")
    private DataGrid bpmEnginesDataGrid;

    @TestComponent(path = "selectButton")
    private Button selectButton;

    @TestComponent(path = "discardButton")
    private Button discardButton;

    /**
     * Opens the context menu for the BPM engine grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openBpmEngineGridContextActions() {
        return openGridContextMenu(bpmEnginesDataGrid);
    }

    /**
     * Find rows in the BPM engine grid by engine name.
     *
     * @param engineName BPM engine name
     * @return found row or throw exception if row not found
     */
    public DataGrid.Row getRowByEngineName(String engineName) {
        return getRowByCellContent(bpmEnginesDataGrid, NAME_COLUMN_INDEX, cell -> {
            String nameBtnText = cell.getCellContent()
                    .find(NAME_BUTTON_BY)
                    .getText();
            return Strings.CI.equals(nameBtnText, engineName);
        });
    }

    /**
     * Find rows in the BPM engine grid by engine URL.
     *
     * @param engineUrl BPM engine URL
     * @return found row or throw exception if row not found
     */
    public DataGrid.Row getRowByEngineUrl(String engineUrl) {
        return getRowByCellContent(bpmEnginesDataGrid, BASE_URL_COLUMN_INDEX, engineUrl);
    }

    /**
     * Click the engine name in the row with the given engine name to open the BPM engine detail view
     * for the existing entity.
     *
     * @param engineName BPM engine name
     * @return wrapper for the opened BPM engine detail view
     */
    public BpmEngineDetailView openBpmEngineDetailView(String engineName) {
        getRowByEngineName(engineName)
                .getCellByIndex(NAME_COLUMN_INDEX)
                .getCellContent()
                .find(NAME_BUTTON_BY)
                .click();

        return $j(BpmEngineDetailView.class).displayed()
                .exists();
    }
}
