/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import io.jmix.masquerade.component.DataGrid;
import org.openqa.selenium.WebElement;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.flowset.control.test_support.ui.UiTestSupport.findRowByCellContents;
import static io.flowset.control.test_support.ui.UiTestSupport.getBodyRows;

/**
 * Asserts at least one visible body row has all specified plain cell texts.
 */
public class BodyRowWithCellTexts extends WebElementCondition {

    private final Map<Integer, String> expected;

    public BodyRowWithCellTexts(Map<Integer, String> expected) {
        super("body row with cell texts " + expected);
        this.expected = ImmutableMap.copyOf(expected);
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        DataGrid dataGrid = GridConditionSupport.resolveGrid(element);
        DataGrid.Row matchedRow = findRowByCellContents(dataGrid, expected);

        if (matchedRow != null) {
            return new CheckResult(true, "matched row with " + expected);
        }

        List<DataGrid.Row> rows = getBodyRows(dataGrid);
        List<Map<Integer, String>> seen = new ArrayList<>();

        for (DataGrid.Row row : rows) {
            seen.add(getRowCellTexts(row));
        }

        return new CheckResult(false, "expected " + expected + "; saw " + seen);
    }

    private Map<Integer, String> getRowCellTexts(DataGrid.Row row) {
        Map<Integer, String> rowTexts = new LinkedHashMap<>();
        for (Integer columnIndex : expected.keySet()) {
            rowTexts.put(columnIndex, row.getCellByIndex(columnIndex)
                    .getCellContent()
                    .getText());
        }
        return rowTexts;
    }
}
