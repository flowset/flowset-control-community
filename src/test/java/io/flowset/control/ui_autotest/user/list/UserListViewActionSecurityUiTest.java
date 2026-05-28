/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.user.list;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.user.TestUserChangePasswordViewRole;
import io.flowset.control.test_support.security.role.user.TestUserCreatePermissionRole;
import io.flowset.control.test_support.security.role.user.TestUserDeletePermissionRole;
import io.flowset.control.test_support.security.role.user.TestUserListAccessRole;
import io.flowset.control.test_support.security.role.user.TestUserResetPasswordViewRole;
import io.flowset.control.test_support.security.role.user.TestUserSubstitutionViewRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.user.UserListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;
import static io.flowset.control.test_support.ui.view.user.UserListView.USERNAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.user.UserListView.USERNAME_COLUMN_INDEX;

@DisplayName("Secured actions on User list view")
@Tag("security")
public class UserListViewActionSecurityUiTest extends AbstractUiTest {

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

    @Autowired
    ControlUiTestingProperties uiProperties;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME like 'test-user%'");
        jdbcTemplate.update(
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in " +
                        "(select ID from SEC_RESOURCE_ROLE where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("createVisibilityOnListViewSource")
    @DisplayName("Create action visibility")
    void givenUserList_whenOpenUserList_thenCreateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-create", "password", roleClass);
        MainView mainView = loginAs("test-user-user-create", "password");

        // when
        UserListView listView = mainView.openUserListView();
        GridContextMenu contextMenu = listView.openUsersGridContextMenu();
        contextMenu.shouldBe(VISIBLE);

        // then
        if (expectedVisible) {
            listView.getCreateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
            contextMenu.find(text("Create")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            listView.getCreateBtn().shouldBe(VISIBLE).shouldNotBe(ENABLED);
            contextMenu.find(text("Create")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("removeVisibilityOnListViewSource")
    @DisplayName("Remove action visibility")
    void givenExistingUser_whenOpenUserList_thenRemoveActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-remove", "password", roleClass);
        MainView mainView = loginAs("test-user-user-remove", "password");

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());
        GridContextMenu contextMenu = listView.openUsersGridContextMenu();
        contextMenu.shouldBe(VISIBLE);

        // then
        if (expectedVisible) {
            listView.getRemoveBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
            contextMenu.find(text("Remove")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            listView.getRemoveBtn().shouldBe(VISIBLE).shouldNotBe(ENABLED);
            contextMenu.find(text("Remove")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilityOnListViewSource")
    @DisplayName("Username link availability")
    void givenExistingUser_whenOpenUserList_thenUsernameLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-detail-link", "password", roleClass);
        MainView mainView = loginAs("test-user-user-detail-link", "password");

        // when
        UserListView listView = mainView.openUserListView();

        // then
        if (expectedVisible) {
            listView.getRowByUsername(uiProperties.getAdminUsername())
                    .getCellByIndex(USERNAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(USERNAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByUsername(uiProperties.getAdminUsername())
                    .getCellByIndex(USERNAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(USERNAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("changePasswordVisibilityOnListViewSource")
    @DisplayName("Change password additional action visibility")
    void givenExistingUser_whenOpenUserList_thenChangePasswordAdditionalActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-change-password", "password", roleClass);
        MainView mainView = loginAs("test-user-user-change-password", "password");

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());

        // then
        if (expectedVisible) {
            listView.openAdditionalDropdown().find(text("Change password")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            listView.openAdditionalDropdown().find(text("Change password")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("changePasswordVisibilityOnListViewSource")
    @DisplayName("Change password context menu action visibility")
    void givenExistingUser_whenOpenUserList_thenChangePasswordContextMenuActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-change-password", "password", roleClass);
        MainView mainView = loginAs("test-user-user-change-password", "password");

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());

        GridContextMenu contextMenu = listView.openUsersGridContextMenu();
        contextMenu.shouldBe(VISIBLE);

        // then
        if (expectedVisible) {
            contextMenu.find(text("Change password")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            contextMenu.find(text("Change password")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("resetPasswordsVisibilityOnListViewSource")
    @DisplayName("Reset passwords additional action visibility")
    void givenExistingUser_whenOpenUserList_thenResetPasswordsAdditionalActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-reset-passwords", "password", roleClass);
        MainView mainView = loginAs("test-user-user-reset-passwords", "password");

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());

        // then
        if (expectedVisible) {
            listView.openAdditionalDropdown().find(text("Reset passwords")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            listView.openAdditionalDropdown().find(text("Reset passwords")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("resetPasswordsVisibilityOnListViewSource")
    @DisplayName("Reset passwords context menu action visibility")
    void givenExistingUser_whenOpenUserList_thenResetPasswordsContextMenuActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-reset-passwords", "password", roleClass);
        MainView mainView = loginAs("test-user-user-reset-passwords", "password");

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());
        GridContextMenu contextMenu = listView.openUsersGridContextMenu();
        contextMenu.shouldBe(VISIBLE);

        // then
        if (expectedVisible) {
            contextMenu.find(text("Reset passwords")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            contextMenu.find(text("Reset passwords")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("userSubstitutionsVisibilityOnListViewSource")
    @DisplayName("User substitutions additional action visibility")
    void givenExistingUser_whenOpenUserList_thenUserSubstitutionsAdditionalActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-substitutions", "password", roleClass);
        MainView mainView = loginAs("test-user-user-substitutions", "password");

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());

        // then
        if (expectedVisible) {
            listView.openAdditionalDropdown().find(text("User substitutions")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            listView.openAdditionalDropdown().find(text("User substitutions")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("userSubstitutionsVisibilityOnListViewSource")
    @DisplayName("User substitutions context menu action visibility")
    void givenExistingUser_whenOpenUserList_thenUserSubstitutionsContextMenuActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-user-substitutions", "password", roleClass);
        MainView mainView = loginAs("test-user-user-substitutions", "password");

        // when
        UserListView listView = mainView.openUserListView();
        listView.selectRowByUsername(uiProperties.getAdminUsername());

        GridContextMenu contextMenu = listView.openUsersGridContextMenu();
        contextMenu.shouldBe(VISIBLE);

        // then
        if (expectedVisible) {
            contextMenu.find(text("User substitutions")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            contextMenu.find(text("User substitutions")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without User list view policy")
    void givenUserWithoutUserListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-user-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-user-no-list-view-policy", "password");

        // when
        open("/users");

        // then
        $j(UserListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'users'"));
    }

    private static Stream<Arguments> createVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without create permission", TestUserListAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with create permission", TestUserCreatePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> removeVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without delete permission", TestUserListAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with delete permission", TestUserDeletePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without user detail view access", TestUserListAccessRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with user detail view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> changePasswordVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("change password without view access", TestUserListAccessRole.class),
                        false
                ),
                Arguments.of(
                        Named.of("change password with view access", TestUserChangePasswordViewRole.class),
                        true
                )
        );
    }

    private static Stream<Arguments> resetPasswordsVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("reset passwords without view access", TestUserListAccessRole.class),
                        false
                ),
                Arguments.of(
                        Named.of("reset passwords with view access", TestUserResetPasswordViewRole.class),
                        true
                )
        );
    }

    private static Stream<Arguments> userSubstitutionsVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("user substitutions without view access", TestUserListAccessRole.class),
                        false
                ),
                Arguments.of(
                        Named.of("user substitutions with view access", TestUserSubstitutionViewRole.class),
                        true
                )
        );
    }
}
