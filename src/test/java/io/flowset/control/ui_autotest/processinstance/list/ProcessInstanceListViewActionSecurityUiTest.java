/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.list;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceActivatePermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceListNoDetailViewRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceListNoProcessDefinitionDetailViewRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceNoProcessDefinitionViewRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceSuspendPermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceTerminatePermissionRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView.ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView.PROCESS_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.ProcessInstanceListView.PROCESS_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Process instance list view")
@Tag("security")
public class ProcessInstanceListViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @DisplayName("Bulk Activate action visibility")
    void givenExistingProcessInstance_whenOpenProcessInstanceList_thenBulkActivateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-instance-bulk-activate",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAs("test-user-process-instance-bulk-activate", "password");

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        GridContextMenu contextMenu = listView.openInstancesGridContextActions();
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
    @MethodSource("bulkSuspendVisibilityOnListViewSource")
    @DisplayName("Bulk Suspend action visibility")
    void givenExistingProcessInstance_whenOpenProcessInstanceList_thenBulkSuspendActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-instance-bulk-suspend",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAs("test-user-process-instance-bulk-suspend", "password");

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        GridContextMenu contextMenu = listView.openInstancesGridContextActions();
        if (expectedVisible) {
            listView.getBulkSuspendBtn().shouldBe(VISIBLE);
            contextMenu.find(text("Suspend")).shouldBe(VISIBLE);
        } else {
            listView.getBulkSuspendBtn().shouldNotBe(VISIBLE);
            contextMenu.find(text("Suspend")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("bulkTerminateVisibilityOnListViewSource")
    @DisplayName("Bulk Terminate action visibility")
    void givenExistingProcessInstance_whenOpenProcessInstanceList_thenBulkTerminateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-instance-bulk-terminate",
                "password",
                roleClass
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        MainView mainView = loginAs("test-user-process-instance-bulk-terminate", "password");

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        GridContextMenu contextMenu = listView.openInstancesGridContextActions();
        if (expectedVisible) {
            listView.getBulkTerminateBtn().shouldBe(VISIBLE);
            contextMenu.find(text("Terminate")).shouldBe(VISIBLE);
        } else {
            listView.getBulkTerminateBtn().shouldNotBe(VISIBLE);
            contextMenu.find(text("Terminate")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processLinkAvailabilityOnListViewSource")
    @DisplayName("Process navigation link availability")
    void givenExistingProcessInstance_whenOpenProcessInstanceList_thenProcessLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getStartedInstances("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser(
                "test-user-process-instance-process-link",
                "password",
                roleClass
        );

        MainView mainView = loginAs("test-user-process-instance-process-link", "password");

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        if (expectedAvailable) {
            listView.getRowByInstanceId(instanceId)
                    .getCellByIndex(PROCESS_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByInstanceId(instanceId)
                    .getCellByIndex(PROCESS_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilityOnListViewSource")
    @DisplayName("Process instance ID link availability")
    void givenExistingProcessInstance_whenOpenProcessInstanceList_thenIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String instanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval")
                .getStartedInstances("vacation_approval")
                .get(0);

        controlTestDataCreator.createUser(
                "test-user-process-instance-detail-link",
                "password",
                roleClass
        );

        MainView mainView = loginAs("test-user-process-instance-detail-link", "password");

        // when
        ProcessInstanceListView listView = mainView.openProcessInstanceListView();

        // then
        if (expectedAvailable) {
            listView.getRowByInstanceId(instanceId)
                    .getCellByIndex(ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByInstanceId(instanceId)
                    .getCellByIndex(ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Process instance list view policy")
    void givenUserWithoutProcessInstanceListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-process-instance-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-process-instance-no-list-view-policy", "password");

        // when
        open("/bpm/process-instances");

        // then
        $j(ProcessInstanceListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/process-instances'"));
    }

    private static Stream<Arguments> bulkActivateVisibilityOnListViewSource() {
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

    private static Stream<Arguments> bulkSuspendVisibilityOnListViewSource() {
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

    private static Stream<Arguments> bulkTerminateVisibilityOnListViewSource() {
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

    private static Stream<Arguments> processLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessDefinitionData view permission", TestProcessInstanceNoProcessDefinitionViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without process definition detail view access",
                                TestProcessInstanceListNoProcessDefinitionDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with ProcessDefinitionData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process instance detail view access",
                                TestProcessInstanceListNoDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with process instance detail view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
