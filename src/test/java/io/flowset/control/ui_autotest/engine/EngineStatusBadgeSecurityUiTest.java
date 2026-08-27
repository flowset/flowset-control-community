/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.engine.TestBpmEngineCreatePermissionRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.main.SelectEnginePopoverContent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static io.jmix.masquerade.JConditions.VISIBLE;

@DisplayName("Secured actions on Engine status badge")
@Tag("security")
public class EngineStatusBadgeSecurityUiTest extends AbstractUiTest {

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

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
    @MethodSource("createEngineVisibilitySource")
    @DisplayName("Add engine button visibility in engine selector popover")
    void givenLoggedInUser_whenOpenEnginePopover_thenAddEngineButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-engine-popover-create", "password", roleClass);

        MainView mainView = loginAs("test-user-engine-popover-create", "password");

        // when
        SelectEnginePopoverContent popover = mainView.getEngineStatusBadge().openSelectEnginePopover();

        // then
        if (expectedVisible) {
            popover.getCreateBpmEngineBtn().shouldBe(VISIBLE);
        } else {
            popover.getCreateBpmEngineBtn().shouldNotBe(VISIBLE);
        }
    }

    private static Stream<Arguments> createEngineVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without create permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with create permission", TestBpmEngineCreatePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
