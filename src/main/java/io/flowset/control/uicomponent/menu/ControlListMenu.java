/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.uicomponent.menu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.component.main.JmixListMenu;
import io.jmix.flowui.kit.component.main.ListMenu;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Side menu component.
 */
public class ControlListMenu extends JmixListMenu {
    public static final String MENU_GROUP_LABEL_CLASS = "menu-group-label";
    private static final Logger log = LoggerFactory.getLogger(ControlListMenu.class);

    /**
     * Adds a menu item to the menu before the existing menu item with the specified id.
     *
     * @param menuItem         the menu item to be added
     * @param beforeMenuItemId the id of the menu item before which the new menu item should be inserted
     */
    public void addMenuItemBefore(MenuItem menuItem, String beforeMenuItemId) {
        MenuItem existingMenuItem = getExistingMenu(beforeMenuItemId);
        if (existingMenuItem != null) {
            int prevItemIdx = rootMenuItems.indexOf(existingMenuItem);
            if (prevItemIdx != -1) {
                addMenuItem(menuItem, prevItemIdx);
            } else {
                addMenuItem(menuItem);
            }
        }
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        if (menuItem instanceof GroupLabelMenuItem groupLabelMenuItem) {
            List<String> relatedMenuItems = groupLabelMenuItem.getChildrenItems();
            if (CollectionUtils.isNotEmpty(relatedMenuItems)) {
                for (String relatedMenuItem : relatedMenuItems) {
                    if (registrations.containsKey(relatedMenuItem)) {
                        addMenuItemBefore(menuItem, relatedMenuItem);
                        return;
                    }
                }
            }
            return;
        }
        super.addMenuItem(menuItem);
    }

    /**
     * Adds a menu item to the menu after the existing menu item with the specified id.
     *
     * @param menuItem        the menu item to be added
     * @param afterMenuItemId the id of the menu item after which the new menu item should be inserted
     */
    public void addMenuItemAfter(MenuItem menuItem, String afterMenuItemId) {
        MenuItem existingMenuItem = getExistingMenu(afterMenuItemId);
        if (existingMenuItem != null) {
            int nextItemIdx = rootMenuItems.indexOf(existingMenuItem);
            if (nextItemIdx == rootMenuItems.size() - 1) {
                addMenuItem(menuItem);
            } else {
                addMenuItem(menuItem, nextItemIdx + 1);
            }
        }

    }

    @Override
    protected ListItem createMenuRecursively(MenuItem menuItem) {
        if (menuItem instanceof GroupLabelMenuItem) {
            checkItemIdDuplicate(menuItem.getId());

            Component groupLabel = new Span(menuItem.getTitle());
            groupLabel.addClassNames(MENU_GROUP_LABEL_CLASS);
            ListItem menuItemComponent = new ListItem(groupLabel);

            registerMenuItem(menuItem, menuItemComponent);

            return menuItemComponent;
        }
        return super.createMenuRecursively(menuItem);
    }

    @Nullable
    protected MenuItem getExistingMenu(String menuId) {
        MenuItem existingMenuItem = getMenuItem(menuId);
        if (existingMenuItem == null) {
            log.debug("Menu item  not found by id '{}'", menuId);
        }

        return existingMenuItem;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class GroupLabelMenuItem extends ListMenu.MenuItem {
        protected List<String> childrenItems;

        public GroupLabelMenuItem(String id) {
            super(id);
        }

        public GroupLabelMenuItem withChildrenItems(String... menuItems) {
            childrenItems = List.of(menuItems);
            return this;
        }
    }
}
