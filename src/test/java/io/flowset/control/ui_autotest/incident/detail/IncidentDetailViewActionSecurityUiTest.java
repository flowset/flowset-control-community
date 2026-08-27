/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.incident.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentListAccessRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentNoExternalTaskViewRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentNoProcessDefinitionViewRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentNoProcessInstanceViewRole;
import io.flowset.control.test_support.security.role.incident.TestIncidentRetryPermissionRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.incident.IncidentDataDetailView;
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
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on incident detail view")
@Tag("security")
public class IncidentDetailViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("retryVisibilityOnDetailViewSource")
    @DisplayName("Retry action visibility on incident detail view")
    void givenExistingRootCauseJobIncident_whenOpenIncidentDetailView_thenRetryActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        controlTestDataCreator.createUser("test-user-incident-detail-retry", "password", roleClass);

        MainView mainView = loginAs("test-user-incident-detail-retry", "password");

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        // then
        if (expectedVisible) {
            detailView.getRetryBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getRetryBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processButtonVisibilityOnDetailViewSource")
    @DisplayName("Process navigation button visibility on incident detail view")
    void givenExistingIncident_whenOpenIncidentDetailView_thenProcessButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        controlTestDataCreator.createUser("test-user-incident-detail-process", "password", roleClass);

        MainView mainView = loginAs("test-user-incident-detail-process", "password");

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        // then
        if (expectedVisible) {
            detailView.getViewProcessBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getViewProcessBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processInstanceButtonVisibilityOnDetailViewSource")
    @DisplayName("Process instance navigation button visibility on incident detail view")
    void givenExistingIncident_whenOpenIncidentDetailView_thenProcessInstanceButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        controlTestDataCreator.createUser("test-user-incident-detail-process-instance", "password", roleClass);

        MainView mainView = loginAs("test-user-incident-detail-process-instance", "password");

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        // then
        if (expectedVisible) {
            detailView.getViewProcessInstanceBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getViewProcessInstanceBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("externalTaskConfigurationBtnVisibilityOnDetailViewSource")
    @DisplayName("Configuration navigation button visibility for an external task incident")
    void givenExternalTaskFailedIncident_whenOpenIncidentDetail_thenConfigurationBtnVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask")
                .getIncidentsByKey("testFailedExternalTask")
                .get(0);

        controlTestDataCreator.createUser("test-user-incident-detail-ext-task", "password", roleClass);

        MainView mainView = loginAs("test-user-incident-detail-ext-task", "password");

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        // then
        if (expectedVisible) {
            detailView.getConfigurationBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getConfigurationBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("configurationButtonVisibilityOnDetailViewSource")
    @DisplayName("Configuration navigation button visibility for a job incident")
    void givenExistingJobFailedIncident_whenOpenIncidentDetailView_thenConfigurationButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);

        controlTestDataCreator.createUser("test-user-incident-detail-configuration", "password", roleClass);

        MainView mainView = loginAs("test-user-incident-detail-configuration", "password");

        // when
        IncidentDataDetailView detailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);

        // then
        if (expectedVisible) {
            detailView.getConfigurationBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getConfigurationBtn().shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Incident detail view policy")
    void givenUserWithoutIncidentDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution()
                .getIncidentsByKey("testFailedJobIncident")
                .get(0);
        controlTestDataCreator.createUser(
                "test-user-incident-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-incident-no-detail-view-policy", "password");

        // when
        open("/bpm/incidents/" + incidentId);

        // then
        $j(IncidentDataDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/incidents/" + incidentId + "'"));
    }

    private static Stream<Arguments> retryVisibilityOnDetailViewSource() {
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

    private static Stream<Arguments> processButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessDefinitionData view permission",
                                TestIncidentNoProcessDefinitionViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ProcessDefinitionData view permission", TestIncidentListAccessRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> processInstanceButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessInstanceData view permission", TestIncidentNoProcessInstanceViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ProcessInstanceData view permission", TestIncidentListAccessRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> externalTaskConfigurationBtnVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ExternalTaskData view permission", TestIncidentNoExternalTaskViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ExternalTaskData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> configurationButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without JobData view permission", TestIncidentListAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with JobData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
