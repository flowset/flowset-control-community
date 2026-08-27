/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.externaltask.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.externaltask.TestExternalTaskNoProcessDefinitionViewRole;
import io.flowset.control.test_support.security.role.externaltask.TestExternalTaskNoProcessInstanceViewRole;
import io.flowset.control.test_support.security.role.externaltask.TestExternalTaskRetryPermissionRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.externaltask.ExternalTaskDataDetailDialog;
import io.flowset.control.test_support.ui.view.externaltask.ExternalTaskDataDetailView;
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
@DisplayName("Secured actions availability on External task detail view")
@Tag("security")
public class ExternalTaskDetailViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("retryActionVisibilitySource")
    @DisplayName("Retry action visibility")
    void givenExistingFailedExternalTask_whenOpenExternalTaskDetailView_thenRetryActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask")
                .getIncidentsByKey("testFailedExternalTask")
                .get(0);

        controlTestDataCreator.createUser("test-user-external-task-detail-retry", "password", roleClass);

        MainView mainView = loginAs("test-user-external-task-detail-retry", "password");

        // when
        IncidentDataDetailView incidentDetailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);
        incidentDetailView.getConfigurationBtn().click();
        ExternalTaskDataDetailDialog detailDialog = $j(ExternalTaskDataDetailDialog.class)
                .exists()
                .displayed();

        // then
        if (expectedVisible) {
            detailDialog.getRetryBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailDialog.getRetryBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processButtonVisibilitySource")
    @DisplayName("Process navigation button visibility")
    void givenExistingExternalTask_whenOpenExternalTaskDetailView_thenProcessButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask")
                .getIncidentsByKey("testFailedExternalTask")
                .get(0);

        controlTestDataCreator.createUser("test-user-external-task-detail-process", "password", roleClass);

        MainView mainView = loginAs("test-user-external-task-detail-process", "password");

        // when
        IncidentDataDetailView incidentDetailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);
        incidentDetailView.getConfigurationBtn().click();
        ExternalTaskDataDetailDialog detailDialog = $j(ExternalTaskDataDetailDialog.class)
                .exists()
                .displayed();

        // then
        if (expectedVisible) {
            detailDialog.getViewProcessBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailDialog.getViewProcessBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processInstanceButtonVisibilitySource")
    @DisplayName("Process instance navigation button visibility")
    void givenExistingExternalTask_whenOpenExternalTaskDetailView_thenProcessInstanceButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String incidentId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask")
                .getIncidentsByKey("testFailedExternalTask")
                .get(0);

        controlTestDataCreator.createUser("test-user-external-task-detail-process-instance", "password", roleClass);

        MainView mainView = loginAs("test-user-external-task-detail-process-instance", "password");

        // when
        IncidentDataDetailView incidentDetailView = mainView.openIncidentListView()
                .openDetailViewByIncidentId(incidentId);
        incidentDetailView.getConfigurationBtn().click();
        ExternalTaskDataDetailDialog detailDialog = $j(ExternalTaskDataDetailDialog.class)
                .exists()
                .displayed();

        // then
        if (expectedVisible) {
            detailDialog.getViewProcessInstanceBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailDialog.getViewProcessInstanceBtn().shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without External task detail view policy")
    void givenUserWithoutExternalTaskDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        String externalTaskId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask")
                .failExternalTasksByKey("testFailedExternalTask")
                .getExternalTasksByKey("testFailedExternalTask")
                .get(0);

        controlTestDataCreator.createUser(
                "test-user-external-task-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-external-task-no-detail-view-policy", "password");

        // when
        open("/bpm/external-tasks/" + externalTaskId);

        // then
        $j(ExternalTaskDataDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/external-tasks/" + externalTaskId + "'"));
    }

    private static Stream<Arguments> retryActionVisibilitySource() {
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

    private static Stream<Arguments> processButtonVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessDefinitionData view permission",
                                TestExternalTaskNoProcessDefinitionViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ProcessDefinitionData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> processInstanceButtonVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessInstanceData view permission",
                                TestExternalTaskNoProcessInstanceViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ProcessInstanceData view permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
