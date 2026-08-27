/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.list.bulkaction;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionActivatePermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDeletePermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionSuspendPermissionRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.ProcessDefinitionListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Bulk actions visibility on Process list view")
@Tag("security")
public class ProcessDefinitionListViewBulkActionSecurityUiTest extends AbstractCamunda7UiTest {

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
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in " +
                        "(select ID from SEC_RESOURCE_ROLE where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("bulkActivateVisibilityOnListViewSource")
    @DisplayName("Bulk Activate action visibility on Process list view")
    void givenExistingProcess_whenOpenProcessList_thenBulkActivateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-bulk-activate",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-bulk-activate", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        GridContextMenu contextMenu = listView.openProcessGridContextActions();
        if (expectedVisible) {
            listView.getBulkActivateBtn().shouldBe(VISIBLE);
            contextMenu.find(text("Activate")).shouldBe(VISIBLE);
        } else {
            listView.getBulkActivateBtn().shouldNotBe(VISIBLE);
            contextMenu.find(text("Activate")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("bulkDeleteVisibilityOnListViewSource")
    @DisplayName("Bulk Delete action visibility on Process list view")
    void givenExistingProcess_whenOpenProcessList_thenBulkDeleteActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-bulk-delete",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-bulk-delete", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        GridContextMenu contextMenu = listView.openProcessGridContextActions();
        if (expectedVisible) {
            listView.getBulkRemoveBtn().shouldBe(VISIBLE);
            contextMenu.find(text("Remove")).shouldBe(VISIBLE);
        } else {
            listView.getBulkRemoveBtn().shouldNotBe(VISIBLE);
            contextMenu.find(text("Remove")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("bulkSuspendVisibilityOnListViewSource")
    @DisplayName("Bulk Suspend action visibility on Process list view")
    void givenExistingActiveProcess_whenOpenProcessList_thenBulkSuspendActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-bulk-suspend",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-bulk-suspend", "password");

        // when
        ProcessDefinitionListView listView = mainView.openProcessListView();

        // then
        GridContextMenu contextMenu = listView.openProcessGridContextActions();
        if (expectedVisible) {
            listView.getBulkSuspendBtn().shouldBe(VISIBLE);
            contextMenu.find(text("Suspend")).shouldBe(VISIBLE);
        } else {
            listView.getBulkSuspendBtn().shouldNotBe(VISIBLE);
            contextMenu.find(text("Suspend")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    private static Stream<Arguments> bulkActivateVisibilityOnListViewSource() {
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

    private static Stream<Arguments> bulkDeleteVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without delete permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with delete permission", TestProcessDefinitionDeletePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> bulkSuspendVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without suspend permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with suspend permission", TestProcessDefinitionSuspendPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
