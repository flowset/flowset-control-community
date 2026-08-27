/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.component;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import io.jmix.masquerade.component.AbstractOverlay;
import io.jmix.masquerade.component.DataGrid;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.flowset.control.test_support.ui.TagNames.BODY;
import static io.flowset.control.test_support.ui.TagNames.CONTEXT_MENU_SELECTOR;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static org.openqa.selenium.By.tagName;

/**
 * Wrapper for the grid context menu.
 * Source component: {@link io.jmix.flowui.kit.component.grid.JmixGridContextMenu}
 */
public class GridContextMenu extends AbstractOverlay<GridContextMenu, DataGrid> {

    public static final By CONTEXT_MENU_LIST_BOX = tagName("vaadin-context-menu-list-box");
    public static final By CONTEXT_MENU_ITEM = tagName("vaadin-context-menu-item");

    public GridContextMenu(DataGrid parentComponent) {
        super(CONTEXT_MENU_SELECTOR, parentComponent);
    }

    @Override
    public ElementsCollection getVisibleElements() {
        return $$(byChained(CONTEXT_MENU_SELECTOR, CONTEXT_MENU_LIST_BOX, CONTEXT_MENU_ITEM))
                .filterBy(VISIBLE);
    }

    /**
     * Finds a visible element in the context menu by the specified condition.
     *
     * @param condition condition to match the menu item
     * @return found the menu item element
     */
    public SelenideElement find(WebElementCondition condition) {
        return getVisibleElements().find(condition);
    }

    /**
     * Closes the context menu by pressing the Escape key on the body element.
     *
     * @return current instance
     */
    public GridContextMenu close() {
        $(BODY).sendKeys(Keys.ESCAPE);
        shouldNotBe(VISIBLE);
        return this;
    }
}
