/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.main;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.view.MainView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.flowset.control.test_support.ui.condition.ControlCondition.menuItemsExactly;

@DisplayName("Main menu items")
public class MainMenuItemsUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Menu items are visible in correct order")
    void givenLoggedInAdmin_whenOpenMainView_thenMenuItemsAreVisibleInCorrectOrder() {
        // when
        MainView mainView = loginAsAdmin();

        // then
        mainView.getListMenu().shouldHave(menuItemsExactly(List.of(
                "MAIN",
                "Dashboard", "Processes", "Process instances", "Incidents", "User tasks",
                "DMN",
                "Decisions", "Decision instances",
                "SYSTEM",
                "Deployments", "Batches", "Administration", "BPM engines", "Users", "Roles",
                "SUPPORT",
                "About"
        )));
    }
}
