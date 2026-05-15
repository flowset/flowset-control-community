/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebElement;

import static io.flowset.control.test_support.ui.UiTestSupport.getBodyRows;
import static io.flowset.control.test_support.ui.condition.grid.GridConditionSupport.resolveGrid;

/**
 * Asserts the count of visible body rows currently rendered in the grid.
 */
public class VisibleBodyRowCount extends WebElementCondition {

    private final int expected;

    public VisibleBodyRowCount(int expected) {
        super("visible body row count = " + expected);
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        int actual = getBodyRows(resolveGrid(element)).size();

        return new CheckResult(actual == expected, actual + " body rows");
    }
}
