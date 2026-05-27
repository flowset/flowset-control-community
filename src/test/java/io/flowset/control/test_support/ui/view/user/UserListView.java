/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.ui.view.user;

import com.codeborne.selenide.ElementsCollection;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.jmix.masquerade.TestComponent;
import io.jmix.masquerade.TestView;
import io.jmix.masquerade.component.Button;
import io.jmix.masquerade.component.DataGrid;
import io.jmix.masquerade.component.DropdownButton;
import io.jmix.masquerade.sys.View;
import lombok.Getter;
import org.openqa.selenium.By;

import static io.flowset.control.test_support.ui.UiTestSupport.*;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Wrapper for the User list view.
 * Source view: {@link io.flowset.control.view.user.UserListView}
 */
@Getter
@TestView(id = "User.list")
public class UserListView extends View<UserListView> {

    public static final By USERNAME_BUTTON_BY = byPath("root", "usernameBtn");

    public static final int USERNAME_COLUMN_INDEX = 0;
    public static final int FIRST_NAME_COLUMN_INDEX = 1;
    public static final int LAST_NAME_COLUMN_INDEX = 2;
    public static final int EMAIL_COLUMN_INDEX = 3;
    public static final int TIME_ZONE_COLUMN_INDEX = 4;
    public static final int ACTIVE_COLUMN_INDEX = 5;

    @TestComponent(path = "createBtn")
    private Button createBtn;

    @TestComponent(path = "removeBtn")
    private Button removeBtn;

    @TestComponent(path = "showRoleAssignmentsBtn")
    private Button showRoleAssignmentsBtn;

    @TestComponent(path = "additionalBtn")
    private DropdownButton additionalBtn;

    @TestComponent(path = "usersDataGrid")
    private DataGrid usersDataGrid;

    /**
     * Opens the context menu for the Users grid.
     *
     * @return opened grid context menu
     */
    public GridContextMenu openUsersGridContextMenu() {
        return openGridContextMenu(usersDataGrid);
    }

    /**
     * Opens the Additional dropdown button menu.
     *
     * @return collection of visible items in the dropdown
     */
    public ElementsCollection openAdditionalDropdown() {
        return getVisibleDropdownItems(additionalBtn.getDelegate());
    }

    /**
     * Finds the row in the grid by the specified username.
     *
     * @param username username
     * @return found row or exception if row not found
     */
    public DataGrid.Row getRowByUsername(String username) {
        return getRowByCellContent(usersDataGrid, USERNAME_COLUMN_INDEX, cell -> {
            String text = cell.getCellContent()
                    .find(USERNAME_BUTTON_BY)
                    .getText();
            return text.equals(username);
        });
    }

    /**
     * Selects the row in the grid by the specified username, clicking a non-link cell so
     * that the click does not trigger detail-view navigation.
     *
     * @param username username
     */
    public void selectRowByUsername(String username) {
        getRowByUsername(username)
                .getCellByIndex(FIRST_NAME_COLUMN_INDEX)
                .getCellContent()
                .click();
    }

    /**
     * Clicks the username link in the row with the given username to open the User detail view.
     *
     * @param username username
     * @return wrapper for the opened User detail view
     */
    public UserDetailView openUserDetailView(String username) {
        getRowByUsername(username)
                .getCellByIndex(USERNAME_COLUMN_INDEX)
                .getCellContent()
                .find(USERNAME_BUTTON_BY)
                .click();
        return $j(UserDetailView.class).exists().displayed();
    }
}
