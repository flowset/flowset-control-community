/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.list;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionActivatePermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDeletePermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDeployPermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailAccessRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionListNoDetailViewRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionMigratePermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionStartPermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionSuspendPermissionRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import io.jmix.masquerade.component.DataGrid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.ACTIONS_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.ACTIVATE_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.KEY_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.KEY_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.NAME_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.PREVIEW_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView.START_PROCESS_BUTTON_BY;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;
import static org.assertj.core.api.Assertions.assertThat;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Process list view")
@Tag("security")
public class ProcessDefinitionListViewActionSecurityUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

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
    @MethodSource("activateVisibilityOnListViewSource")
    @DisplayName("Activate action visibility on Process list view")
    void givenExistingSuspendedProcess_whenOpenProcessList_thenActivateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-activate",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);

        MainView mainView = loginAs("test-user-process-activate", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");

        // then
        if (expectedVisible) {
            processRow.getCellByIndex(ACTIONS_COLUMN_INDEX)
                    .getCellContent()
                    .find(ACTIVATE_BUTTON_BY)
                    .shouldBe(VISIBLE);
        } else {
            processRow.getCellByIndex(ACTIONS_COLUMN_INDEX)
                    .getCellContent()
                    .find(ACTIVATE_BUTTON_BY)
                    .shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("suspendVisibilityOnListViewSource")
    @DisplayName("Suspend action visibility on Process list view")
    void givenExistingActiveProcess_whenOpenProcessList_thenSuspendActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-suspend",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-suspend", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");
        List<String> availableActions = listView.openOtherActions(processRow).texts();

        // then
        if (expectedVisible) {
            assertThat(availableActions).contains("Suspend");
        } else {
            assertThat(availableActions).doesNotContain("Suspend");
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("startVisibilityOnListViewSource")
    @DisplayName("Start action visibility on Process list view")
    void givenExistingActiveProcess_whenOpenProcessList_thenStartActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-start",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-start", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");

        // then
        if (expectedVisible) {
            processRow.getCellByIndex(ACTIONS_COLUMN_INDEX)
                    .getCellContent()
                    .find(START_PROCESS_BUTTON_BY)
                    .shouldBe(VISIBLE);
        } else {
            processRow.getCellByIndex(ACTIONS_COLUMN_INDEX)
                    .getCellContent()
                    .find(START_PROCESS_BUTTON_BY)
                    .shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("migrateVisibilityOnListViewSource")
    @DisplayName("Migrate action visibility on Process list view")
    void givenExistingProcess_whenOpenProcessList_thenMigrateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-migrate",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-migrate", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");
        List<String> availableActions = listView.openOtherActions(processRow).texts();

        // then
        if (expectedVisible) {
            assertThat(availableActions).contains("Migrate");
        } else {
            assertThat(availableActions).doesNotContain("Migrate");
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("deleteVisibilityOnListViewSource")
    @DisplayName("Delete action visibility on Process list view")
    void givenExistingProcess_whenOpenProcessList_thenDeleteActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-delete",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-delete", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();
        DataGrid.Row processRow = listView.getRowByProcessKey("vacation_approval");
        List<String> availableActions = listView.openOtherActions(processRow).texts();

        // then
        if (expectedVisible) {
            assertThat(availableActions).contains("Remove");
        } else {
            assertThat(availableActions).doesNotContain("Remove");
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("deployVisibilityOnListViewSource")
    @DisplayName("Deploy action visibility on Process list view")
    void givenExistingProcess_whenOpenProcessList_thenDeployActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-deploy",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-deploy", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        if (expectedVisible) {
            listView.getDeployBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            listView.getDeployBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilityOnListViewSource")
    @DisplayName("Name and key links availability on Process list view")
    void givenExistingProcess_whenOpenProcessList_thenDetailLinksAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        controlTestDataCreator.createUser("test-user-process-detail-link", "password", roleClass);

        MainView mainView = loginAs("test-user-process-detail-link", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        if (expectedEnabled) {
            listView.getRowByProcessKey("vacation_approval")
                    .getCellByIndex(NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
            listView.getRowByProcessKey("vacation_approval")
                    .getCellByIndex(KEY_COLUMN_INDEX)
                    .getCellContent()
                    .find(KEY_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByProcessKey("vacation_approval")
                    .getCellByIndex(NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
            listView.getRowByProcessKey("vacation_approval")
                    .getCellByIndex(KEY_COLUMN_INDEX)
                    .getCellContent()
                    .find(KEY_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Process list view policy")
    void givenUserWithoutProcessListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-process-no-list-view-policy", "password");

        // when
        open("/bpm/process-definitions");

        // then
        $j(ProcessDefinitionListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/process-definitions'"));
    }

    @Test
    @DisplayName("Preview diagram action visibility on Process list view")
    void givenExistingProcess_whenOpenProcessList_thenPreviewActionIsVisible() {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-preview",
                "password",
                TestProcessDefinitionDetailAccessRole.class
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-preview", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        listView.getRowByProcessKey("vacation_approval")
                .getCellByIndex(KEY_COLUMN_INDEX)
                .getCellContent()
                .find(PREVIEW_BUTTON_BY)
                .shouldBe(VISIBLE);
    }

    private static Stream<Arguments> activateVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without activate permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with activate permission", TestProcessDefinitionActivatePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> suspendVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without suspend permission", TestProcessDefinitionDeletePermissionRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with suspend permission", TestProcessDefinitionSuspendPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> startVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without start permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with start permission", TestProcessDefinitionStartPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> migrateVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without migrate permission", TestProcessDefinitionDeletePermissionRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with migrate permission", TestProcessDefinitionMigratePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> deleteVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without delete permission", TestProcessDefinitionSuspendPermissionRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with delete permission", TestProcessDefinitionDeletePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }


    private static Stream<Arguments> deployVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without deploy permission", TestProcessDefinitionDetailAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with deploy permission", TestProcessDefinitionDeployPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process definition detail view access",
                                TestProcessDefinitionListNoDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with process definition detail view access",
                                TestProcessDefinitionDetailAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }

}
