/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui;

import org.openqa.selenium.By;

import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

/**
 * Contains predefined {@link By} selectors for UI elements used in tests.
 */
public class TagNames {
    public static final By BODY = tagName("body");
    public static final By GRID_COLUMN_SORTER_BY = tagName("vaadin-grid-sorter");
    public static final By CONTEXT_MENU_SELECTOR = tagName("vaadin-context-menu-overlay");
    public static final By MENU_BAR_LIST_BOX = tagName("vaadin-menu-bar-list-box");
    public static final By VAADIN_LOADING_INDICATOR = cssSelector("vaadin-connection-indicator[loading='']");
    public static final By NOTIFICATION_CARD = tagName("vaadin-notification-card");
    public static final By VAADIN_TAB = By.tagName("vaadin-tab");
    public static final By VAADIN_BUTTON = By.tagName("vaadin-button");
}
