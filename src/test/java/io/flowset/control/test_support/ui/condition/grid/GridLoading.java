/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.grid;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import io.flowset.control.test_support.ui.TagNames;
import org.openqa.selenium.WebElement;

/**
 * Asserts the grid is still loading either by showing the {@code "Loading..."} placeholder
 * or by having an active {@code <vaadin-connection-indicator>}.
 */
public class GridLoading extends WebElementCondition {

    public GridLoading() {
        super("grid loading");
    }

    @Override
    public CheckResult check(Driver driver, WebElement element) {
        boolean loadingTextVisible = element.getText().contains("Loading...");
        boolean indicatorActive = !driver.getWebDriver().findElements(TagNames.VAADIN_LOADING_INDICATOR).isEmpty();
        boolean loading = loadingTextVisible || indicatorActive;

        String actualValue = "loadingTextVisible=" + loadingTextVisible + ", indicatorActive=" + indicatorActive;
        return new CheckResult(loading, actualValue);
    }
}
