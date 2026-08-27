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

import java.util.ArrayList;
import java.util.List;

import static io.flowset.control.test_support.ui.UiTestSupport.getBodyRows;

/**
 * Asserts at least one visible body row has plain text {@code expected} in the cell
 * at {@code columnIndex}.
 */
public class AnyBodyRowsHaveCellText extends WebElementCondition {

    private final int columnIndex;
    private final String expected;

    public AnyBodyRowsHaveCellText(int columnIndex, String expected) {
        super("any body row: cell[" + columnIndex + "] = \"" + expected + "\"");
        this.columnIndex = columnIndex;
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<DataGrid.Row> rows = getBodyRows(GridConditionSupport.resolveGrid(element));
        List<String> seen = new ArrayList<>();
        for (DataGrid.Row row : rows) {
            String actual = row.getCellByIndex(columnIndex).getCellContent().getText();
            seen.add(actual);
            if (actual.equals(expected)) {
                return new CheckResult(true, "matched, saw " + seen);
            }
        }
        return new CheckResult(false, "no row matched; saw " + seen);
    }
}
