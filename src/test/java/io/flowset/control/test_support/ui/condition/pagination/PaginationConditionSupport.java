/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.condition.pagination;

import org.openqa.selenium.By;

import static org.openqa.selenium.By.cssSelector;

/**
 * Shared selectors for pagination-scoped {@link com.codeborne.selenide.WebElementCondition}s.
 * Mirrors the selectors used by
 * {@link io.flowset.control.test_support.ui.component.SimplePagination}.
 */
public class PaginationConditionSupport {

    public static final By TOTAL_COUNT_LABEL = cssSelector(".jmix-simple-pagination-total-count");

}
