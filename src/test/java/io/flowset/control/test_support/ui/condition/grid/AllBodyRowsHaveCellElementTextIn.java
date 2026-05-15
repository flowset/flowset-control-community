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

import java.util.Collection;
import java.util.List;

import static io.flowset.control.test_support.ui.UiTestSupport.getBodyRows;

/**
 * Asserts every visible body row has a child element matching {@code selector}
 * with text in the cell at {@code columnIndex} that is a member of {@code expected}.
 */
public class AllBodyRowsHaveCellElementTextIn extends WebElementCondition {

    private final int columnIndex;
    private final By selector;
    private final Collection<String> expected;

    public AllBodyRowsHaveCellElementTextIn(int columnIndex, By selector, Collection<String> expected) {
        super("all body rows: cell[" + columnIndex + "] " + selector + " in " + expected);
        this.columnIndex = columnIndex;
        this.selector = selector;
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<DataGrid.Row> rows = getBodyRows(GridConditionSupport.resolveGrid(element));
        if (rows.isEmpty()) {
            return new CheckResult(expected.isEmpty(), "no visible body rows; expected " + expected);
        }

        for (int i = 0; i < rows.size(); i++) {
            String actual = rows.get(i).getCellByIndex(columnIndex).getCellContent().find(selector).getText();
            if (!expected.contains(actual)) {
                return new CheckResult(false,
                        "row " + i + " text=\"" + actual + "\" not in " + expected);
            }
        }
        return new CheckResult(true, rows.size() + " rows match");
    }
}
