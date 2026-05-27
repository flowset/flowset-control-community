/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.component.DataGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selectors.shadowCss;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.flowset.control.test_support.ui.TagNames.MENU_BAR_LIST_BOX;
import static io.flowset.control.test_support.ui.TagNames.VAADIN_LOADING_INDICATOR;
import static io.flowset.control.test_support.ui.condition.ControlCondition.gridLoading;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.Masquerade.UI_TEST_ID;
import static io.jmix.masquerade.sys.TagNames.MENU_BAR_ITEM;
import static io.jmix.masquerade.sys.TagNames.MENU_BAR_OVERLAY;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

/**
 * Contains help methods for UI tests like finding rows by cell content, finding visible context menu items, etc.
 */
public class UiTestSupport {
    private static final Logger log = LoggerFactory.getLogger(UiTestSupport.class);

    public static final int DATA_LOADING_WAIT_DURATION_SEC = Integer.getInteger("flowset.control.testing.ui.dataLoadingWaitSec", 10);
    public static final int DATA_SAVING_WAIT_DURATION_SEC = Integer.getInteger("flowset.control.testing.ui.dataSavingWaitSec", 10);
    public static final int MENU_ITEM_OPENING_WAIT_DURATION_SEC = Integer.getInteger("flowset.control.testing.ui.menuItemOpeningWaitSec", 5);
    public static final int LOGIN_OPEN_WAIT_DURATION_SEC = Integer.getInteger("flowset.control.testing.ui.loginOpenWaitSec", 5);
    public static final int LOGIN_WAIT_DURATION_SEC = Integer.getInteger("flowset.control.testing.ui.loginWaitSec", 5);
    public static final int TEST_ENGINE_CONNECTION_WAIT_DURATION_SEC = Integer.getInteger("flowset.control.testing.ui.testEngineConnectionWaitSec", 30);

    /**
     * Opens the context menu for the given data grid by right-clicking on it.
     *
     * @param dataGrid the data grid to open the context menu for
     * @return the GridContextMenu instance representing the opened context menu
     */
    public static GridContextMenu openGridContextMenu(DataGrid dataGrid) {
        dataGrid.exists()
                .displayed()
                .getDelegate()
                .contextClick();

        return new GridContextMenu(dataGrid)
                .shouldBe(VISIBLE);
    }

    /**
     * Finds header cells from the first row of the specified data grid.
     *
     * @param dataGrid the data grid to find header cells from
     * @return list of header cells
     */
    public static List<DataGrid.Cell> getHeaderCells(DataGrid dataGrid) {
        return dataGrid
                .getHeaderRow(0)
                .getDelegate()
                .findAll(tagName("th"))
                .filterBy(VISIBLE)
                .stream()
                .map(selenideElement -> new DataGrid.Cell(selenideElement, dataGrid))
                .toList();
    }

    /**
     * Retrieves visible dropdown items for a given dropdown button.
     *
     * @param dropdown dropdown button
     * @return collection of visible dropdown item elements.
     */
    public static ElementsCollection getVisibleDropdownItems(SelenideElement dropdown) {
        dropdown.$(byChained(cssSelector("[last-visible]")))
                .shouldBe(ENABLED)
                .shouldBe(VISIBLE)
                .click();

        return $$(byChained(MENU_BAR_OVERLAY, MENU_BAR_LIST_BOX, MENU_BAR_ITEM))
                .filterBy(VISIBLE);
    }

    /**
     * Finds a row in the grid by the content of a cell using the specified predicate.
     *
     * @param dataGrid             a DataGrid to search within.
     * @param cellIdx              index of the cell to check for content.
     * @param cellContentPredicate predicate to test cell content.
     * @return found row, or {@code null} if no row matches the predicate (also when the grid is empty).
     */
    public static DataGrid.Row findRowByCellContent(DataGrid dataGrid, int cellIdx, Predicate<DataGrid.Cell> cellContentPredicate) {
        dataGrid.exists()
                .displayed();
        waitUntilDataLoading(dataGrid);

        ElementsCollection rows = getVisibleBodyRows(dataGrid);

        if (rows.isEmpty()) {
            log.info("No rows found in the grid");
            return null;
        }

        log.info("Found {} rows in the grid", rows.size());

        for (SelenideElement row : rows) {
            ElementsCollection cells = row.findAll(tagName("td"))
                    .filterBy(VISIBLE);

            if (cells.size() <= cellIdx) {
                log.debug("Row has no visible cell with index {}", cellIdx);
                continue;
            }

            DataGrid.Cell cellByIndex = new DataGrid.Cell(cells.get(cellIdx), dataGrid);

            log.debug("Checking cell content idx: {}", cellIdx);
            if (cellContentPredicate.test(cellByIndex)) {
                return new DataGrid.Row(row, dataGrid);
            }
        }
        return null;
    }

    /**
     * Finds a row in the grid by the content of a cell using the specified content value.
     *
     * @param dataGrid    a DataGrid to search within.
     * @param cellIdx     index of the cell to check for content.
     * @param cellContent content value to match.
     * @return found row, or {@code null} if no row matches.
     */
    public static DataGrid.Row findRowByCellContent(DataGrid dataGrid, int cellIdx, String cellContent) {
        return findRowByCellContent(dataGrid, cellIdx, cell -> {
            SelenideElement cellContentEl = cell.getCellContent();
            String text = cellContentEl.getText();
            log.info("Checking text {} in cell content text: {}", cellContent, text);
            return text.equals(cellContent);
        });
    }

    /**
     * Finds a row in the grid by the content of a cell using the specified predicate.
     * Throws {@link RuntimeException} if the grid is empty or no row matches.
     *
     * @param dataGrid             a DataGrid to search within.
     * @param cellIdx              index of the cell to check for content.
     * @param cellContentPredicate predicate to test cell content.
     * @return found row.
     * @throws RuntimeException if no row matches.
     */
    public static DataGrid.Row getRowByCellContent(DataGrid dataGrid, int cellIdx, Predicate<DataGrid.Cell> cellContentPredicate) {
        DataGrid.Row row = findRowByCellContent(dataGrid, cellIdx, cellContentPredicate);
        if (row == null) {
            throw new RuntimeException("No row with cell content found in the grid");
        }
        return row;
    }

    /**
     * Finds a row in the grid by the content of a cell using the specified content value.
     * Throws {@link RuntimeException} if no row matches.
     *
     * @param dataGrid    a DataGrid to search within.
     * @param cellIdx     index of the cell to check for content.
     * @param cellContent content value to match.
     * @return found row.
     * @throws RuntimeException if no row matches.
     */
    public static DataGrid.Row getRowByCellContent(DataGrid dataGrid, int cellIdx, String cellContent) {
        DataGrid.Row row = findRowByCellContent(dataGrid, cellIdx, cellContent);
        if (row == null) {
            throw new RuntimeException("No row with cell content '" + cellContent + "' found in the grid");
        }
        return row;
    }

    public static void waitUntilPageLoading() {
        $(VAADIN_LOADING_INDICATOR).shouldNot(exist,
                Duration.ofSeconds(DATA_LOADING_WAIT_DURATION_SEC));
    }

    /**
     * Returns all visible rows of the DataGrid body.
     *
     * @param dataGrid a DataGrid to get body rows from.
     * @return list of DataGrid.Row objects representing body rows.
     */
    public static List<DataGrid.Row> getBodyRows(DataGrid dataGrid) {
        ElementsCollection rows = getVisibleBodyRows(dataGrid);

        log.info("Found {} rows in the grid", rows.size());

        return rows.stream()
                .map(row -> new DataGrid.Row(row, dataGrid)
                        .displayed())
                .toList();
    }

    /**
     * Finds a row in the grid by the content of multiple cells using the specified content values.
     *
     * @param dataGrid     a DataGrid to search within.
     * @param cellContents map of cell indices to content values to match.
     * @return found row, or {@code null} if no row matches all cell contents.
     */
    public static DataGrid.Row findRowByCellContents(DataGrid dataGrid, Map<Integer, String> cellContents) {
        dataGrid.exists()
                .displayed();

        waitUntilDataLoading(dataGrid);

        ElementsCollection rows = getVisibleBodyRows(dataGrid).snapshot();

        if (rows.isEmpty()) {
            log.debug("No rows found in the grid");
            return null;
        }

        int idx = 0;
        for (SelenideElement row : rows) {
            boolean allCellsFound = false;
            for (Map.Entry<Integer, String> entry : cellContents.entrySet()) {
                int cellIdx = entry.getKey();
                String cellContent = entry.getValue();

                DataGrid.Cell cellByIndex = dataGrid.getCellByIndex(idx, cellIdx)
                        .exists()
                        .displayed();

                String text = cellByIndex.getCellContent().getText();
                log.debug("Checking row '{}' for string '{}', cell content text: {}", idx, cellContent, text);
                if (!text.equals(cellContent)) {
                    break;
                }
                allCellsFound = true;
            }
            if (allCellsFound) {
                return new DataGrid.Row(row, dataGrid);
            }

            idx++;
        }
        return null;
    }

    /**
     * Returns all visible table rows of the DataGrid body.
     *
     * @param dataGrid the data grid to retrieve visible rows from
     * @return ElementsCollection of visible rows
     */
    public static ElementsCollection getVisibleBodyRows(DataGrid dataGrid) {
        dataGrid.exists()
                .displayed();

        waitUntilDataLoading(dataGrid);

        return $(shadowCss("tbody[id='items']", getGridSelector(dataGrid)))
                .findAll(cssSelector("[part~='body-row']"))
                .filterBy(VISIBLE);
    }


    /**
     * Finds a row in the grid by the content of multiple cells.
     * Throws {@link RuntimeException} if no row matches.
     *
     * @param dataGrid     a DataGrid to search within.
     * @param cellContents map of cell indices to content values to match.
     * @return found row.
     * @throws RuntimeException if no row matches.
     */
    public static DataGrid.Row getRowByCellContents(DataGrid dataGrid, Map<Integer, String> cellContents) {
        DataGrid.Row row = findRowByCellContents(dataGrid, cellContents);
        if (row == null) {
            throw new IllegalArgumentException("No row with cell contents '" + cellContents.entrySet() + "' found in the grid");
        }
        return row;
    }

    /**
     * Waits up to {@link #DATA_LOADING_WAIT_DURATION_SEC} seconds for all grid-related loading
     * signals to disappear, including the DataGrid's {@code "Loading..."} placeholder and the
     * global {@code <vaadin-connection-indicator>} active state ({@code loading=""}).
     *
     * @param dataGrid the DataGrid to wait for loading to complete.
     */
    public static void waitUntilDataLoading(DataGrid dataGrid) {
        dataGrid.shouldNotHave(gridLoading, Duration.ofSeconds(DATA_LOADING_WAIT_DURATION_SEC));
    }

    private static String getGridSelector(DataGrid dataGrid) {
        String gridTestId = dataGrid.getDelegate().getDomAttribute(UI_TEST_ID);

        if (gridTestId == null || gridTestId.isBlank()) {
            throw new IllegalArgumentException("DataGrid delegate has no '%s' DOM attribute".formatted(UI_TEST_ID));
        }

        return "vaadin-grid[%s='%s']".formatted(UI_TEST_ID, gridTestId);
    }
}
