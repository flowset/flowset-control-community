/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.externaltask.TestExternalTaskRetryPermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceDetailNoLinkedDetailViewRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment;
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
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment.EXTERNAL_TASK_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.ExternalTasksTabFragment.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions on External tasks tab in Process instance detail view")
@Tag("security")
public class ExternalTasksTabSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("retryVisibilitySource")
    @DisplayName("Retry action visibility")
    void givenExistingExternalTask_whenOpenRuntimeExternalTasksTab_thenRetryActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-external-task-tab", "password", roleClass);
        MainView mainView = loginAs("test-user-process-instance-external-task-tab", "password");

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        // then
        GridContextMenu contextMenu = externalTasksTab.openTasksGridContextMenu();
        if (expectedVisible) {
            externalTasksTab.getRetryButton().shouldBe(VISIBLE);
            contextMenu.find(text("Retry")).shouldBe(VISIBLE);
        } else {
            externalTasksTab.getRetryButton().shouldNotBe(VISIBLE);
            contextMenu.find(text("Retry")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("External task ID link availability")
    void givenExistingExternalTask_whenOpenRuntimeExternalTasksTab_thenExternalTaskIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");
        String instanceId = dataManager.getStartedInstances("testFailedExternalTask").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-external-task-detail-link-tab", "password", roleClass);
        MainView mainView = loginAs("test-user-process-instance-external-task-detail-link-tab", "password");

        // when
        ExternalTasksTabFragment externalTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeExternalTasksTab();

        // then
        if (expectedEnabled) {
            externalTasksTab.getRowByActivityId("failedExternalTask")
                    .getCellByIndex(EXTERNAL_TASK_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            externalTasksTab.getRowByActivityId("failedExternalTask")
                    .getCellByIndex(EXTERNAL_TASK_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    private static Stream<Arguments> retryVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without retry permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with retry permission", TestExternalTaskRetryPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without external task detail dialog view access",
                                TestProcessInstanceDetailNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with external task detail dialog view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
