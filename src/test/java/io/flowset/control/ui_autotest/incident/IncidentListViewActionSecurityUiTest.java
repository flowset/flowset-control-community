/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.incident;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.incident.TestIncidentListNoLinkedDetailViewRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentListAccessRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentNoProcessDefinitionViewRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentNoProcessInstanceViewRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentRetryPermissionRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.incident.IncidentListView;
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

import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.ACTIONS_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.INCIDENT_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.INLINE_RETRY_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.PROCESS_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.PROCESS_DEFINITION_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.PROCESS_INSTANCE_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.incident.IncidentListView.PROCESS_INSTANCE_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on incident list view")
@Tag("security")
public class IncidentListViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("bulkRetryVisibilityOnListViewSource")
    @DisplayName("Bulk Retry action visibility on incident list view")
    void givenExistingIncident_whenOpenIncidentListView_thenBulkRetryActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-incident-bulk-retry", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAs("test-user-incident-bulk-retry", "password");

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        GridContextMenu contextMenu = listView.openIncidentsGridContextMenu();
        if (expectedVisible) {
            listView.getBulkRetryButton().shouldBe(VISIBLE);
            contextMenu.find(text("Retry")).shouldBe(VISIBLE);
        } else {
            listView.getBulkRetryButton().shouldNotBe(VISIBLE);
            contextMenu.find(text("Retry")).shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("inlineRetryVisibilityOnListViewSource")
    @DisplayName("Inline Retry action visibility on incident list view")
    void givenExistingRootCauseJobIncident_whenOpenIncidentListView_thenInlineRetryActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-incident-inline-retry", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        MainView mainView = loginAs("test-user-incident-inline-retry", "password");

        // when
        IncidentListView listView = mainView.openIncidentListView();
        DataGrid.Row row = listView.getRowByActivityId("throwsExceptionTask");

        // then
        if (expectedVisible) {
            row.getCellByIndex(ACTIONS_COLUMN_INDEX)
                    .getCellContent()
                    .find(INLINE_RETRY_BUTTON_BY)
                    .shouldBe(VISIBLE);
        } else {
            row.getCellByIndex(ACTIONS_COLUMN_INDEX)
                    .getCellContent()
                    .find(INLINE_RETRY_BUTTON_BY)
                    .shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("incidentLinkAvailabilityOnListViewSource")
    @DisplayName("Incident navigation link availability on incident list view")
    void givenExistingIncident_whenOpenIncidentListView_thenIncidentLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        controlTestDataCreator.createUser("test-user-incident-id-link", "password", roleClass);

        MainView mainView = loginAs("test-user-incident-id-link", "password");

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        if (expectedAvailable) {
            listView.getRowByIncidentId(incidentId)
                    .getCellByIndex(INCIDENT_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByIncidentId(incidentId)
                    .getCellByIndex(INCIDENT_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processInstanceLinkAvailabilityOnListViewSource")
    @DisplayName("Process instance navigation link availability on incident list view")
    void givenExistingIncident_whenOpenIncidentListView_thenProcessInstanceLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        controlTestDataCreator.createUser("test-user-incident-process-instance-link", "password", roleClass);

        MainView mainView = loginAs("test-user-incident-process-instance-link", "password");

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        if (expectedAvailable) {
            listView.getRowByIncidentId(incidentId)
                    .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_INSTANCE_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByIncidentId(incidentId)
                    .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_INSTANCE_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processLinkAvailabilityOnListViewSource")
    @DisplayName("Process navigation link availability on incident list view")
    void givenExistingIncident_whenOpenIncidentListView_thenProcessLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        controlTestDataCreator.createUser("test-user-incident-process-link", "password", roleClass);

        MainView mainView = loginAs("test-user-incident-process-link", "password");

        // when
        IncidentListView listView = mainView.openIncidentListView();

        // then
        if (expectedAvailable) {
            listView.getRowByIncidentId(incidentId)
                    .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByIncidentId(incidentId)
                    .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Incident list view policy")
    void givenUserWithoutIncidentListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-incident-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-incident-no-list-view-policy", "password");

        // when
        open("/bpm/incidents");

        // then
        $j(IncidentListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/incidents'"));
    }

    private static Stream<Arguments> bulkRetryVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without retry permission", TestIncidentListAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with retry permission", TestIncidentRetryPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> inlineRetryVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without retry permission", TestIncidentListAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with retry permission", TestIncidentRetryPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> processInstanceLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessInstanceData view permission", TestIncidentNoProcessInstanceViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without process instance detail view access",
                                TestIncidentListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with ProcessInstanceData view permission", TestIncidentListAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> processLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessDefinitionData view permission",
                                TestIncidentNoProcessDefinitionViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without process definition detail view access",
                                TestIncidentListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with ProcessDefinitionData view permission", TestIncidentListAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> incidentLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without incident detail view access", TestIncidentListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with incident detail view access", TestIncidentListAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
