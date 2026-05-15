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

import java.util.List;

import static io.flowset.control.test_support.ui.UiTestSupport.getBodyRows;

/**
 * Asserts the visible body rows have, in order, plain cell texts at {@code columnIndex}
 * equal to {@code expected}. Implicitly asserts size — fails if row count differs.
 */
public class BodyCellTextsExactly extends WebElementCondition {

    private final int columnIndex;
    private final List<String> expected;

    public BodyCellTextsExactly(int columnIndex, List<String> expected) {
        super("body row cell[" + columnIndex + "] texts = " + expected);
        this.columnIndex = columnIndex;
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<DataGrid.Row> rows = getBodyRows(GridConditionSupport.resolveGrid(element));
        List<String> actual = rows.stream()
                .map(r -> r.getCellByIndex(columnIndex).getCellContent().getText())
                .toList();
        return new CheckResult(actual.equals(expected), "got " + actual);
    }
}
