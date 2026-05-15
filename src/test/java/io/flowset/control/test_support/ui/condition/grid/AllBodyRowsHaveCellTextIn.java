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

import java.util.Collection;
import java.util.List;

import static io.flowset.control.test_support.ui.UiTestSupport.getBodyRows;

/**
 * Asserts every visible body row has plain text in the cell at {@code columnIndex}
 * that is a member of {@code expected}. Vacuously true for an empty grid.
 */
public class AllBodyRowsHaveCellTextIn extends WebElementCondition {

    private final int columnIndex;
    private final Collection<String> expected;

    public AllBodyRowsHaveCellTextIn(int columnIndex, Collection<String> expected) {
        super("all body rows: cell[" + columnIndex + "] in " + expected);
        this.columnIndex = columnIndex;
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<DataGrid.Row> rows = getBodyRows(GridConditionSupport.resolveGrid(element));
        for (int i = 0; i < rows.size(); i++) {
            String actual = rows.get(i).getCellByIndex(columnIndex).getCellContent().getText();
            if (!expected.contains(actual)) {
                return new CheckResult(false,
                        "row " + i + " text=\"" + actual + "\" not in " + expected);
            }
        }
        return new CheckResult(true, rows.size() + " rows match");
    }
}
