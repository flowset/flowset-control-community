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
 * Asserts the visible body rows have, in order, child-element ({@code selector})
 * texts at {@code columnIndex} equal to {@code expected}. Implicitly asserts size.
 */
public class BodyCellElementTextsExactly extends WebElementCondition {

    private final int columnIndex;
    private final By selector;
    private final List<String> expected;

    public BodyCellElementTextsExactly(int columnIndex, By selector, List<String> expected) {
        super("body row cell[" + columnIndex + "] " + selector + " texts = " + expected);
        this.columnIndex = columnIndex;
        this.selector = selector;
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<DataGrid.Row> rows = getBodyRows(GridConditionSupport.resolveGrid(element));
        List<String> actual = rows.stream()
                .map(r -> r.getCellByIndex(columnIndex).getCellContent().find(selector).getText())
                .toList();
        return new CheckResult(actual.equals(expected), "got " + actual);
    }
}
