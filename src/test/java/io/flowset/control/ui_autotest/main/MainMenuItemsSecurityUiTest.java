/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.main;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.security.role.main.TestMainMenuAboutRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuBatchesRole;
import io.flowset.control.security.UiMinimalRole;
import org.junit.jupiter.api.Test;
import io.flowset.control.test_support.security.role.main.TestMainMenuBpmEngineRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuDashboardRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuDecisionDefinitionRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuDecisionInstanceRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuDeploymentsRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuIncidentsRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuNoItemsRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuProcessDefinitionRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuProcessInstanceRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuResourceRoleModelRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuUserRole;
import io.flowset.control.test_support.security.role.main.TestMainMenuUserTasksRole;
import io.flowset.control.test_support.ui.view.MainView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Stream;

import static io.flowset.control.test_support.ui.condition.ControlCondition.menuItemsExactly;

@DisplayName("Main menu items visibility")
@Tag("security")
public class MainMenuItemsSecurityUiTest extends AbstractUiTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDownUsers() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME like 'test-user%'");
        jdbcTemplate.update(
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in (select ID from SEC_RESOURCE_ROLE " +
                        "where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("mainGroupSource")
    @DisplayName("Main group visibility matches permissions")
    void givenUserPermissions_whenOpenMainView_thenMainGroupVisibilityMatchesPermissions(
            Class<?> roleClass, List<String> expectedItems
    ) {
        // given
        controlTestDataCreator.createUser("test-user-main-menu-group", "password", roleClass);

        // when
        MainView mainView = loginAs("test-user-main-menu-group", "password");

        // then
        mainView.getListMenu().shouldHave(menuItemsExactly(expectedItems));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("dmnGroupSource")
    @DisplayName("DMN group visibility matches permissions")
    void givenUserPermissions_whenOpenMainView_thenDmnGroupVisibilityMatchesPermissions(
            Class<?> roleClass, List<String> expectedItems
    ) {
        // given
        controlTestDataCreator.createUser("test-user-main-menu-group", "password", roleClass);

        // when
        MainView mainView = loginAs("test-user-main-menu-group", "password");

        // then
        mainView.getListMenu().shouldHave(menuItemsExactly(expectedItems));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("systemGroupSource")
    @DisplayName("System group visibility matches permissions")
    void givenUserPermissions_whenOpenMainView_thenSystemGroupVisibilityMatchesPermissions(
            Class<?> roleClass, List<String> expectedItems
    ) {
        // given
        controlTestDataCreator.createUser("test-user-main-menu-group", "password", roleClass);

        // when
        MainView mainView = loginAs("test-user-main-menu-group", "password");

        // then
        mainView.getListMenu().shouldHave(menuItemsExactly(expectedItems));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("supportGroupSource")
    @DisplayName("Support group visibility matches permissions")
    void givenUserPermissions_whenOpenMainView_thenSupportGroupVisibilityMatchesPermissions(
            Class<?> roleClass, List<String> expectedItems
    ) {
        // given
        controlTestDataCreator.createUser("test-user-main-menu-group", "password", roleClass);

        // when
        MainView mainView = loginAs("test-user-main-menu-group", "password");

        // then
        mainView.getListMenu().shouldHave(menuItemsExactly(expectedItems));
    }

    static Stream<Arguments> mainGroupSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("no Main items available", TestMainMenuNoItemsRole.class),
                        List.of()
                ),
                Arguments.of(
                        Named.of("MAIN + Dashboard available", TestMainMenuDashboardRole.class),
                        List.of("MAIN", "Dashboard")
                ),
                Arguments.of(
                        Named.of("MAIN + Processes available", TestMainMenuProcessDefinitionRole.class),
                        List.of("MAIN", "Processes")
                ),
                Arguments.of(
                        Named.of("MAIN + Process instances available", TestMainMenuProcessInstanceRole.class),
                        List.of("MAIN", "Process instances")
                ),
                Arguments.of(
                        Named.of("MAIN + Incidents available", TestMainMenuIncidentsRole.class),
                        List.of("MAIN", "Incidents")
                ),
                Arguments.of(
                        Named.of("MAIN + User tasks available", TestMainMenuUserTasksRole.class),
                        List.of("MAIN", "User tasks")
                )
        );
    }

    static Stream<Arguments> dmnGroupSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("no DMN items available", TestMainMenuNoItemsRole.class),
                        List.of()
                ),
                Arguments.of(
                        Named.of("DMN + Decisions available", TestMainMenuDecisionDefinitionRole.class),
                        List.of("DMN", "Decisions")
                ),
                Arguments.of(
                        Named.of("DMN + Decision instances available", TestMainMenuDecisionInstanceRole.class),
                        List.of("DMN", "Decision instances")
                )
        );
    }

    static Stream<Arguments> systemGroupSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("no System items available", TestMainMenuNoItemsRole.class),
                        List.of()
                ),
                Arguments.of(
                        Named.of("SYSTEM + Deployments available", TestMainMenuDeploymentsRole.class),
                        List.of("SYSTEM", "Deployments")
                ),
                Arguments.of(
                        Named.of("SYSTEM + Batches available", TestMainMenuBatchesRole.class),
                        List.of("SYSTEM", "Batches")
                ),
                Arguments.of(
                        Named.of("SYSTEM + Administration + BPM engines available", TestMainMenuBpmEngineRole.class),
                        List.of("SYSTEM", "Administration", "BPM engines")
                ),
                Arguments.of(
                        Named.of("SYSTEM + Administration + Users available", TestMainMenuUserRole.class),
                        List.of("SYSTEM", "Administration", "Users")
                ),
                Arguments.of(
                        Named.of("SYSTEM + Administration + Roles available", TestMainMenuResourceRoleModelRole.class),
                        List.of("SYSTEM", "Administration", "Roles")
                )
        );
    }

    static Stream<Arguments> supportGroupSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("no Support items available", TestMainMenuNoItemsRole.class),
                        List.of()
                ),
                Arguments.of(
                        Named.of("About available", TestMainMenuAboutRole.class),
                        List.of("SUPPORT", "About")
                )
        );
    }

    @Test
    @DisplayName("UI minimal role: only Support group with About item is visible")
    void givenUserWithUiMinimalRole_whenOpenMainView_thenOnlySupportGroupWithAboutIsVisible() {
        // given
        controlTestDataCreator.createUser("test-user-main-menu-group", "password", UiMinimalRole.class);

        // when
        MainView mainView = loginAs("test-user-main-menu-group", "password");

        // then
        mainView.getListMenu().shouldHave(menuItemsExactly(List.of("SUPPORT", "About")));
    }
}
