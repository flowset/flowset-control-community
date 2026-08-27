/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.pagination;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebElement;

/**
 * Asserts the simple-pagination total-count label shows the resolved numeric
 * value {@code expected} — the state renders after the user clicks the
 * {@code "[?]"} placeholder and the total count is computed.
 */
public class TotalCount extends WebElementCondition {

    private final int expected;

    public TotalCount(int expected) {
        super("total count = " + expected);
        this.expected = expected;
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        String text = element.findElement(PaginationConditionSupport.TOTAL_COUNT_LABEL).getText();
        return new CheckResult(String.valueOf(expected).equals(text), "text=\"" + text + "\"");
    }
}
