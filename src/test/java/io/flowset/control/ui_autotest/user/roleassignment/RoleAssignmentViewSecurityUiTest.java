/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.user.roleassignment;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.view.user.RoleAssignmentView;
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

@DisplayName("Security on Role assignment view")
@Tag("security")
public class RoleAssignmentViewSecurityUiTest extends AbstractUiTest {

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
    @DisplayName("Direct URL navigation is denied without Role assignments view policy")
    void givenUserWithoutRoleAssignmentsViewPolicy_whenOpenViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-no-role-assignments-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-no-role-assignments-view-policy", "password");

        // when
        open("/sec/roleassignment/" + uiTestingProperties.getAdminUsername());

        // then
        $j(RoleAssignmentView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text(
                "Could not navigate to 'sec/roleassignment/" + uiTestingProperties.getAdminUsername() + "'"
        ));
    }
}
