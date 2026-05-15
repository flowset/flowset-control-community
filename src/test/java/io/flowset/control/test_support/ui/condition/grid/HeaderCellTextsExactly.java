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

import static io.flowset.control.test_support.ui.UiTestSupport.getHeaderCells;
import static io.flowset.control.test_support.ui.condition.grid.GridConditionSupport.resolveGrid;

/**
 * Asserts the visible header cell texts equal {@code expected} in order.
 */
public class HeaderCellTextsExactly extends WebElementCondition {

    private final List<String> expected;

    public HeaderCellTextsExactly(List<String> expected) {
        super("non-empty header cell texts (in order) = " + expected);
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<DataGrid.Cell> cells = getHeaderCells(resolveGrid(element));

        List<String> actual = cells.stream()
                .map(c -> c.getCellContent().getText())
                .toList();

        return new CheckResult(actual.equals(expected), "got " + actual);
    }
}
