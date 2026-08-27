/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Asserts the body-row count of a Vaadin Grid by reading the inner shadow
 * {@code <table>}'s {@code aria-rowcount} attribute, which Vaadin maintains
 * as {@code bodyRows + 1} (the {@code + 1} is the header row).
 */
public class GridBodyRowCount extends WebElementCondition {

    private final int expected;

    public GridBodyRowCount(int expected) {
        super("grid body row count = " + expected);
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        WebElement table = element.getShadowRoot().findElement(By.cssSelector("table"));
        String raw = table.getDomAttribute("aria-rowcount");
        int actual = raw == null ? 0 : Math.max(0, Integer.parseInt(raw) - 1);
        return new CheckResult(actual == expected,
                "aria-rowcount=" + raw + " (= " + actual + " body rows)");
    }
}
