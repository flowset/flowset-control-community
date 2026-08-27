/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.engine.TestBpmEngineCreatePermissionRole;
import io.flowset.control.test_support.security.role.engine.TestBpmEngineDeletePermissionRole;
import io.flowset.control.test_support.security.role.engine.TestBpmEngineListNoDetailViewRole;
import io.flowset.control.test_support.security.role.engine.TestEngineMarkAsDefaultPermissionRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineListView;
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

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.text;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.flowset.control.test_support.ui.view.engine.BpmEngineListView.ACTIONS_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.engine.BpmEngineListView.MARK_AS_DEFAULT_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.engine.BpmEngineListView.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.engine.BpmEngineListView.NAME_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Secured actions on BPM engine list view")
@Tag("security")
public class BpmEngineListViewSecurityUiTest extends AbstractUiTest {

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
        jdbcTemplate.update("delete from CONTROL_BPM_ENGINE where NAME like '%test-engine%'");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("createVisibilityOnListViewSource")
    @DisplayName("Create action visibility")
    void givenEngineList_whenOpenEngineList_thenCreateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-bpm-engine-create", "password", roleClass);

        MainView mainView = loginAs("test-user-bpm-engine-create", "password");

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // then
        GridContextMenu contextMenu = listView.openBpmEngineGridContextActions();
        if (expectedVisible) {
            listView.getCreateButton().shouldBe(VISIBLE).shouldBe(ENABLED);
            contextMenu.find(text("Create")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            listView.getCreateButton().shouldBe(VISIBLE).shouldNotBe(ENABLED);
            contextMenu.find(text("Create")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("removeVisibilityOnListViewSource")
    @DisplayName("Remove action visibility")
    void givenExistingBpmEngine_whenOpenEngineList_thenRemoveActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-bpm-engine-remove", "password", roleClass);
        controlTestDataCreator.createRandomBpmEngine("test-engine-remove-" + System.currentTimeMillis(), false);

        MainView mainView = loginAs("test-user-bpm-engine-remove", "password");

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();
        listView.getBpmEnginesDataGrid().clickSelectAll();

        // then
        GridContextMenu contextMenu = listView.openBpmEngineGridContextActions();
        if (expectedVisible) {
            listView.getRemoveButton().shouldBe(VISIBLE).shouldBe(ENABLED);
            contextMenu.find(text("Remove")).shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            listView.getRemoveButton().shouldBe(VISIBLE).shouldNotBe(ENABLED);
            contextMenu.find(text("Remove")).shouldBe(VISIBLE).shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("markAsDefaultVisibilityOnListViewSource")
    @DisplayName("Mark as default action visibility")
    void givenExistingNonDefaultBpmEngine_whenOpenEngineList_thenMarkAsDefaultActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-bpm-engine-mark-default", "password", roleClass);

        controlTestDataCreator.createRandomBpmEngine("default-test-engine-" + System.currentTimeMillis(), true);

        String nonDefaultEngineName = "non-default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(nonDefaultEngineName, false);

        MainView mainView = loginAs("test-user-bpm-engine-mark-default", "password");

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // then
        if (expectedVisible) {
            listView.getRowByEngineName(nonDefaultEngineName)
                    .getCellByIndex(ACTIONS_COLUMN_INDEX)
                    .getCellContent()
                    .find(MARK_AS_DEFAULT_BUTTON_BY)
                    .shouldBe(VISIBLE);
        } else {
            listView.getRowByEngineName(nonDefaultEngineName)
                    .getCellByIndex(ACTIONS_COLUMN_INDEX)
                    .getCellContent()
                    .find(MARK_AS_DEFAULT_BUTTON_BY)
                    .shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilityOnListViewSource")
    @DisplayName("Engine name link availability")
    void givenExistingBpmEngine_whenOpenEngineList_thenNameLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-bpm-engine-detail-link", "password", roleClass);
        String engineName = "detail-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, false);

        MainView mainView = loginAs("test-user-bpm-engine-detail-link", "password");

        // when
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // then
        if (expectedVisible) {
            listView.getRowByEngineName(engineName)
                    .getCellByIndex(NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByEngineName(engineName)
                    .getCellByIndex(NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without BPM engine list view policy")
    void givenUserWithoutBpmEngineListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-bpm-engine-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-bpm-engine-no-list-view-policy", "password");

        // when
        open("/bpm/engines");

        // then
        $j(BpmEngineListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/engines'"));
    }

    private static Stream<Arguments> createVisibilityOnListViewSource() {
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

    private static Stream<Arguments> removeVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without delete permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with delete permission", TestBpmEngineDeletePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> markAsDefaultVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without mark as default permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with mark as default permission", TestEngineMarkAsDefaultPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without BPM engine detail view access", TestBpmEngineListNoDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with BPM engine detail view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
