/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import io.jmix.masquerade.component.DataGrid;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static io.flowset.control.test_support.ui.UiTestSupport.getBodyRows;

/**
 * Asserts every visible body row has a child element matching {@code selector}
 * (e.g. an inner button) with text {@code expected} in the cell at {@code columnIndex}.
 */
public class AllBodyRowsHaveCellElementText extends WebElementCondition {

    private final int columnIndex;
    private final By selector;
    private final String expected;

    public AllBodyRowsHaveCellElementText(int columnIndex, By selector, String expected) {
        super("all body rows: cell[" + columnIndex + "] " + selector + " = \"" + expected + "\"");
        this.columnIndex = columnIndex;
        this.selector = selector;
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<DataGrid.Row> rows = getBodyRows(GridConditionSupport.resolveGrid(element));
        if (rows.isEmpty()) {
            return new CheckResult(false, "no visible body rows");
        }

        for (int i = 0; i < rows.size(); i++) {
            String actual = rows.get(i).getCellByIndex(columnIndex).getCellContent().find(selector).getText();
            if (!actual.equals(expected)) {
                return new CheckResult(false, "row " + i + " text=\"" + actual + "\"");
            }
        }
        return new CheckResult(true, rows.size() + " rows match");
    }
}
