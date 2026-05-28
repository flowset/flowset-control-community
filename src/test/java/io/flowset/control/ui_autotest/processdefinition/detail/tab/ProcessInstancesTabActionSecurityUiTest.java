/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail.tab;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceActivatePermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceSuspendPermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceTerminatePermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailNoProcessInstanceDetailViewRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment;
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
import static io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessInstancesTabFragment.ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JConditions.ENABLED;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Process instances tab")
@Tag("security")
public class ProcessInstancesTabActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("activateVisibilitySource")
    @DisplayName("Activate action visibility")
    void givenExistingProcess_whenOpenProcessInstancesTab_thenActivateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-instance-activate",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAs("test-user-instance-activate", "password");

        // when
        ProcessInstancesTabFragment instancesTab = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        // then
        GridContextMenu contextMenu = instancesTab.openInstancesGridContextMenu();
        if (expectedVisible) {
            instancesTab.getActivateButton().shouldBe(VISIBLE);
            contextMenu.find(text("Activate")).shouldBe(VISIBLE);
        } else {
            instancesTab.getActivateButton().shouldNotBe(VISIBLE);
            contextMenu.find(text("Activate")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("suspendVisibilitySource")
    @DisplayName("Suspend action visibility")
    void givenExistingProcess_whenOpenProcessInstancesTab_thenSuspendActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-instance-suspend",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAs("test-user-instance-suspend", "password");

        // when
        ProcessInstancesTabFragment instancesTab = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        // then
        GridContextMenu contextMenu = instancesTab.openInstancesGridContextMenu();
        if (expectedVisible) {
            instancesTab.getSuspendButton().shouldBe(VISIBLE);
            contextMenu.find(text("Suspend")).shouldBe(VISIBLE);
        } else {
            instancesTab.getSuspendButton().shouldNotBe(VISIBLE);
            contextMenu.find(text("Suspend")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("terminateVisibilitySource")
    @DisplayName("Terminate action visibility")
    void givenExistingProcess_whenOpenProcessInstancesTab_thenTerminateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-instance-terminate",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAs("test-user-instance-terminate", "password");

        // when
        ProcessInstancesTabFragment instancesTab = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        // then
        GridContextMenu contextMenu = instancesTab.openInstancesGridContextMenu();
        if (expectedVisible) {
            instancesTab.getTerminateButton().shouldBe(VISIBLE);
            contextMenu.find(text("Terminate")).shouldBe(VISIBLE);
        } else {
            instancesTab.getTerminateButton().shouldNotBe(VISIBLE);
            contextMenu.find(text("Terminate")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("Process instance ID link availability")
    void givenExistingProcess_whenOpenProcessInstancesTab_thenProcessInstanceIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        controlTestDataCreator.createUser(
                "test-user-instance-detail-link",
                "password",
                roleClass
        );

        MainView mainView = loginAs("test-user-instance-detail-link", "password");

        // when
        ProcessInstancesTabFragment instancesTab = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .openProcessInstancesTab();

        // then
        if (expectedEnabled) {
            instancesTab.getProcessInstancesGrid()
                    .getRowByIndex(0)
                    .getCellByIndex(ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            instancesTab.getProcessInstancesGrid()
                    .getRowByIndex(0)
                    .getCellByIndex(ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    private static Stream<Arguments> activateVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without activate permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with activate permission", TestProcessInstanceActivatePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> suspendVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without suspend permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with suspend permission", TestProcessInstanceSuspendPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> terminateVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without terminate permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with terminate permission", TestProcessInstanceTerminatePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process instance detail view access",
                                TestProcessDefinitionDetailNoProcessInstanceDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with process instance detail view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
