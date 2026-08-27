/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebElement;

import static com.codeborne.selenide.Condition.text;

/**
 * Asserts the grid renders the {@code "No data"} placeholder — the empty data grid state text
 */
public class EmptyGrid extends WebElementCondition {

    public EmptyGrid() {
        super("empty grid (\"No data\")");
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        return text("No data").check(driver, element);
    }
}
