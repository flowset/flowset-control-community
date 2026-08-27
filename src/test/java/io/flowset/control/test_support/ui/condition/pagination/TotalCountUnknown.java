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
 * Asserts the simple-pagination total-count label shows the {@code "[?]"}
 * placeholder — the unresolved state Vaadin renders before the user clicks
 * the placeholder to compute the total count.
 */
public class TotalCountUnknown extends WebElementCondition {

    private static final String UNKNOWN_PLACEHOLDER = "[?]";

    public TotalCountUnknown() {
        super("total count unknown (\"" + UNKNOWN_PLACEHOLDER + "\")");
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        String text = element.findElement(PaginationConditionSupport.TOTAL_COUNT_LABEL).getText();
        return new CheckResult(UNKNOWN_PLACEHOLDER.equals(text), "text=\"" + text + "\"");
    }
}
