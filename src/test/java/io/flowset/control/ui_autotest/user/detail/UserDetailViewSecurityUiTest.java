/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.user.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.view.user.UserDetailView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Security on User detail view")
@Tag("security")
public class UserDetailViewSecurityUiTest extends AbstractUiTest {

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

    @Test
    @DisplayName("Direct URL navigation is denied without User detail view policy")
    void givenUserWithoutUserDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        String userId = jdbcTemplate.queryForObject(
                "select ID from USER_ where USERNAME = ?",
                String.class,
                uiTestingProperties.getAdminUsername()
        );
        controlTestDataCreator.createUser(
                "test-user-no-user-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-no-user-detail-view-policy", "password");

        // when
        open("/users/" + userId);

        // then
        $j(UserDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'users/" + userId + "'"));
    }

    @Test
    @DisplayName("Direct URL navigation is denied without User create view policy")
    void givenUserWithoutUserCreateViewPolicy_whenOpenCreateViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-no-user-create-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-no-user-create-view-policy", "password");

        // when
        open("/users/new");

        // then
        $j(UserDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'users/new'"));
    }
}
