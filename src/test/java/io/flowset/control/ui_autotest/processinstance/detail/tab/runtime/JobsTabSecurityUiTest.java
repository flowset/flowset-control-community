/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.runtime;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.job.TestJobActivatePermissionRole;
import io.flowset.control.test_support.security.role.job.TestJobRetryPermissionRole;
import io.flowset.control.test_support.security.role.job.TestJobSuspendPermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceDetailNoLinkedDetailViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment;
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
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processinstance.detail.tab.JobsTabFragment.JOB_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions on Jobs tab in Process instance detail view")
@Tag("security")
public class JobsTabSecurityUiTest extends AbstractCamunda7UiTest {

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
    void givenExistingJob_whenOpenRuntimeJobsTab_thenRetryActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-job-retry-tab", "password", roleClass);
        MainView mainView = loginAs("test-user-process-instance-job-retry-tab", "password");

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        GridContextMenu contextMenu = jobsTab.openJobsGridContextMenu();
        if (expectedVisible) {
            jobsTab.getRetryButton().shouldBe(VISIBLE);
            contextMenu.find(text("Retry")).shouldBe(VISIBLE);
        } else {
            jobsTab.getRetryButton().shouldNotBe(VISIBLE);
            contextMenu.find(text("Retry")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("activateVisibilitySource")
    @DisplayName("Activate action visibility")
    void givenExistingJob_whenOpenRuntimeJobsTab_thenActivateActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-job-activate-tab", "password", roleClass);
        MainView mainView = loginAs("test-user-process-instance-job-activate-tab", "password");

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        GridContextMenu contextMenu = jobsTab.openJobsGridContextMenu();
        if (expectedVisible) {
            jobsTab.getActivateButton().shouldBe(VISIBLE);
            contextMenu.find(text("Activate")).shouldBe(VISIBLE);
        } else {
            jobsTab.getActivateButton().shouldNotBe(VISIBLE);
            contextMenu.find(text("Activate")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("suspendVisibilitySource")
    @DisplayName("Suspend action visibility")
    void givenExistingJob_whenOpenRuntimeJobsTab_thenSuspendActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-job-suspend-tab", "password", roleClass);
        MainView mainView = loginAs("test-user-process-instance-job-suspend-tab", "password");

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        GridContextMenu contextMenu = jobsTab.openJobsGridContextMenu();
        if (expectedVisible) {
            jobsTab.getSuspendButton().shouldBe(VISIBLE);
            contextMenu.find(text("Suspend")).shouldBe(VISIBLE);
        } else {
            jobsTab.getSuspendButton().shouldNotBe(VISIBLE);
            contextMenu.find(text("Suspend")).shouldNotBe(VISIBLE);
        }
        contextMenu.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("Job ID link availability")
    void givenExistingJob_whenOpenRuntimeJobsTab_thenJobIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        controlTestDataCreator.createUser("test-user-process-instance-job-detail-link-tab", "password", roleClass);
        MainView mainView = loginAs("test-user-process-instance-job-detail-link-tab", "password");

        // when
        JobsTabFragment jobsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openRuntimeJobsTab();

        // then
        if (expectedEnabled) {
            jobsTab.getRowByFailedActivityId("throwsExceptionTask")
                    .getCellByIndex(JOB_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            jobsTab.getRowByFailedActivityId("throwsExceptionTask")
                    .getCellByIndex(JOB_ID_COLUMN_INDEX)
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
                        Named.of("with retry permission", TestJobRetryPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> activateVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without activate permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with activate permission", TestJobActivatePermissionRole.class),
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
                        Named.of("with suspend permission", TestJobSuspendPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without job detail dialog view access", TestProcessInstanceDetailNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with job detail dialog view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
