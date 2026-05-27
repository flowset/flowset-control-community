/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.user.list;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.texts;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.view.user.UserListView.USERNAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.user.UserListView.USERNAME_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JConditions.visibleItems;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Actions on User list view")
public class UserListViewActionsUiTest extends AbstractUiTest {

    @Autowired
    ControlUiTestingProperties uiProperties;

    @Test
    @DisplayName("List of available actions on User list view")
    void givenExistingUser_whenOpenUserListView_thenAllActionsAvailable() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();

        // then
        listView.getCreateBtn().shouldBe(VISIBLE);
        listView.getRemoveBtn().shouldBe(VISIBLE);
        listView.getShowRoleAssignmentsBtn().shouldBe(VISIBLE);
        listView.getAdditionalBtn().shouldBe(VISIBLE);

        listView.openAdditionalDropdown()
                .shouldHave(size(3))
                .shouldHave(texts("Change password", "Reset passwords", "User substitutions"));

        $("body").sendKeys(Keys.ESCAPE);

        listView.openUsersGridContextMenu()
                .shouldHave(visibleItems("Create", "Edit", "Remove",
                        "Role assignments", "Change password", "Reset passwords", "User substitutions"));
    }



    @Test
    @DisplayName("Create action opens the User detail view")
    void givenExistingUser_whenClickCreate_thenUserDetailViewOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();
        listView.getCreateBtn().click();

        // then
        webdriver().shouldHave(urlContaining("/users/new"));

        $j(UserDetailView.class).exists().displayed();
    }

    @Test
    @DisplayName("Username link in data grid opens the User detail view")
    void givenExistingUser_whenUsernameLinkClicked_thenUserDetailViewOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();
        listView.getRowByUsername(uiProperties.getAdminUsername())
                .getCellByIndex(USERNAME_COLUMN_INDEX)
                .getCellContent()
                .find(USERNAME_BUTTON_BY)
                .click();

        // then
        webdriver().shouldHave(urlContaining("/users/"));

        UserDetailView detailView = $j(UserDetailView.class).exists().displayed();
        detailView.getUsernameField().shouldHave(value(uiProperties.getAdminUsername()));
    }

    @Test
    @DisplayName("Double-clicking a user row opens the User detail view")
    void givenExistingUser_whenRowDoubleClicked_thenUserDetailViewOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();
        listView.getRowByUsername(uiProperties.getAdminUsername())
                .getDelegate()
                .doubleClick();

        // then
        webdriver().shouldHave(urlContaining("/users/"));

        UserDetailView detailView = $j(UserDetailView.class).exists().displayed();
        detailView.getUsernameField().shouldHave(value(uiProperties.getAdminUsername()));
    }

    @Test
    @DisplayName("Remove action opens the confirmation dialog")
    void givenExistingUser_whenClickRemove_thenConfirmationDialogOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());
        listView.getRemoveBtn().click();

        // then
        JmixDialog dialog = $j(JmixDialog.class, JmixDialog.OVERLAY).exists().displayed();
        dialog.getDelegate()
                .shouldHave(text("Are you sure you want to delete selected elements?"));
    }

    @Test
    @DisplayName("Show role assignments action opens the Role assignment view")
    void givenExistingUser_whenClickShowRoleAssignments_thenRoleAssignmentViewOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());
        listView.getShowRoleAssignmentsBtn().click();

        // then
        webdriver().shouldHave(urlContaining("/sec/roleassignment/" + uiProperties.getAdminUsername()));

        $j(RoleAssignmentView.class).exists().displayed();
    }

    @Test
    @DisplayName("Change password action opens the Change password dialog")
    void givenExistingUser_whenClickChangePassword_thenChangePasswordDialogOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());
        listView.openAdditionalDropdown()
                .find(text("Change password"))
                .click();

        // then
        $j(ChangePasswordDialog.class).exists().displayed();
    }

    @Test
    @DisplayName("Reset password action opens the Reset password dialog")
    void givenExistingUser_whenClickResetPassword_thenResetPasswordDialogOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());
        listView.openAdditionalDropdown()
                .find(text("Reset passwords"))
                .click();

        // then
        $j(ResetPasswordDialog.class).exists().displayed();
    }

    @Test
    @DisplayName("Show user substitutions action opens the User substitution view")
    void givenExistingUser_whenClickShowUserSubstitutions_thenUserSubstitutionViewOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());
        listView.openAdditionalDropdown()
                .find(text("User substitutions"))
                .click();

        // then
        webdriver().shouldHave(urlContaining("/sec/usersubstitution/" + uiProperties.getAdminUsername()));

        $j(UserSubstitutionView.class).exists().displayed();
    }
}
