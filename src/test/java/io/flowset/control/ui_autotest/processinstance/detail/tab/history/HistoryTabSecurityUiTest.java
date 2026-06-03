/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processinstance.detail.tab.history;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceHistoryTabsAccessRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceDetailNoLinkedDetailViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryActivitiesTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryDecisionsTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryIncidentsTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryUserTasksTabFragment;
import io.flowset.control.test_support.ui.view.processinstance.detail.tab.HistoryVariablesTabFragment;
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

import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions on sub-tabs in History tab on Process instance detail view")
@Tag("security")
public class HistoryTabSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("Activity instance ID link availability in Activities tab")
    void givenExistingProcessInstance_whenOpenHistoryActivitiesTab_thenActivityInstanceIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        controlTestDataCreator.createUser("test-user-history-activity", "password", roleClass);
        MainView mainView = loginAs("test-user-history-activity", "password");

        // when
        HistoryActivitiesTabFragment activitiesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryActivitiesTab();

        // then
        if (expectedEnabled) {
            activitiesTab.getRowByActivityId("approveVacationTask")
                    .getCellByIndex(HistoryActivitiesTabFragment.ACTIVITY_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryActivitiesTabFragment.ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            activitiesTab.getRowByActivityId("approveVacationTask")
                    .getCellByIndex(HistoryActivitiesTabFragment.ACTIVITY_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryActivitiesTabFragment.ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("Incident ID link availability in Incidents tab")
    void givenExistingProcessInstanceWithHistoricIncident_whenOpenHistoryIncidentsTab_thenIncidentIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();
        String instanceId = dataManager.getStartedInstances("testFailedJobIncident").get(0);

        controlTestDataCreator.createUser("test-user-history-incident", "password", roleClass);
        MainView mainView = loginAs("test-user-history-incident", "password");

        // when
        HistoryIncidentsTabFragment incidentsTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryIncidentsTab();

        // then
        if (expectedEnabled) {
            incidentsTab.getRowByActivityId("throwsExceptionTask")
                    .getCellByIndex(HistoryIncidentsTabFragment.INCIDENT_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryIncidentsTabFragment.ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            incidentsTab.getRowByActivityId("throwsExceptionTask")
                    .getCellByIndex(HistoryIncidentsTabFragment.INCIDENT_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryIncidentsTabFragment.ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("Task ID link availability in User tasks tab")
    void givenExistingProcessInstanceWithUserTask_whenOpenHistoryUserTasksTab_thenTaskIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");
        String instanceId = dataManager.getStartedInstances("vacation_approval").get(0);

        controlTestDataCreator.createUser("test-user-history-user-task", "password", roleClass);
        MainView mainView = loginAs("test-user-history-user-task", "password");

        // when
        HistoryUserTasksTabFragment userTasksTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryUserTasksTab();

        // then
        if (expectedEnabled) {
            userTasksTab.getRowByTaskDefinitionKey("approveVacationTask")
                    .getCellByIndex(HistoryUserTasksTabFragment.TASK_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryUserTasksTabFragment.ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            userTasksTab.getRowByTaskDefinitionKey("approveVacationTask")
                    .getCellByIndex(HistoryUserTasksTabFragment.TASK_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryUserTasksTabFragment.ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("Variable name link availability in Variables tab")
    void givenExistingProcessInstanceWithVariable_whenOpenHistoryVariablesTab_thenVariableNameLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build());
        String instanceId = dataManager.getStartedInstances("testUpdateVariable").get(0);

        controlTestDataCreator.createUser("test-user-history-variable", "password", roleClass);
        MainView mainView = loginAs("test-user-history-variable", "password");

        // when
        HistoryVariablesTabFragment variablesTab = mainView.openProcessInstanceListView()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryVariablesTab();

        // then
        if (expectedEnabled) {
            variablesTab.getRowByVariableName("firstVariable")
                    .getCellByIndex(HistoryVariablesTabFragment.NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryVariablesTabFragment.NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            variablesTab.getRowByVariableName("firstVariable")
                    .getCellByIndex(HistoryVariablesTabFragment.NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryVariablesTabFragment.NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilitySource")
    @DisplayName("Decision instance ID link availability in Decisions tab")
    void givenExistingProcessInstanceWithDecision_whenOpenHistoryDecisionsTab_thenDecisionInstanceIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        CamundaSampleDataManager dataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision");
        String instanceId = dataManager.getStartedInstances("testProcessWithDecision").get(0);

        controlTestDataCreator.createUser("test-user-history-decision", "password", roleClass);
        MainView mainView = loginAs("test-user-history-decision", "password");

        // when
        HistoryDecisionsTabFragment decisionsTab = mainView.openProcessInstanceListView()
                .switchToAllViewMode()
                .openDetailViewByInstanceId(instanceId)
                .openHistoryDecisionsTab();

        // then
        if (expectedEnabled) {
            decisionsTab.getRowByActivityId("evaluateDecisionTask")
                    .getCellByIndex(HistoryDecisionsTabFragment.DECISION_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryDecisionsTabFragment.ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            decisionsTab.getRowByActivityId("evaluateDecisionTask")
                    .getCellByIndex(HistoryDecisionsTabFragment.DECISION_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(HistoryDecisionsTabFragment.ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    private static Stream<Arguments> detailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without target detail dialog view access",
                                TestProcessInstanceDetailNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with target detail dialog view access",
                                TestProcessInstanceHistoryTabsAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
