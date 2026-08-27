/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.pagination;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Asserts the simple-pagination total-count label is not visible — either absent
 * from the DOM or rendered with {@code display: none}. Vaadin hides the placeholder
 * when all rows fit on a single page.
 */
public class TotalCountHidden extends WebElementCondition {

    public TotalCountHidden() {
        super("total count label hidden");
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        List<WebElement> labels = element.findElements(PaginationConditionSupport.TOTAL_COUNT_LABEL);
        boolean hidden = labels.isEmpty() || !labels.get(0).isDisplayed();
        return new CheckResult(hidden, hidden ? "hidden" : "displayed");
    }
}
