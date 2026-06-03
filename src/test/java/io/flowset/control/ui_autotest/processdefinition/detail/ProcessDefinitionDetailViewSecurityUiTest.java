/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.processdefinition.*;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.ProcessDefinitionDetailView;
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
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions on Process detail view")
@Tag("security")
public class ProcessDefinitionDetailViewSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("activateVisibilityOnDetailViewSource")
    @DisplayName("Activate action visibility on Process detail view")
    void givenExistingSuspendedProcessDefinition_whenOpenDetailView_thenActivateActionVisibilityMatchesPermissions(
            Class<?> roleClass,
            boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-process-activate", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .suspendByKey("vacation_approval", false);

        MainView mainView = loginAs("test-user-process-activate", "password");

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getActivateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getGeneralPanel().getActivateBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("suspendVisibilityOnDetailViewSource")
    @DisplayName("Suspend action visibility on Process detail view")
    void givenExistingActiveProcessDefinition_whenOpenDetailView_thenSuspendActionVisibilityMatchesPermissions(
            Class<?> roleClass,
            boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-process-suspend", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-suspend", "password");

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getSuspendBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getGeneralPanel().getSuspendBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("startVisibilityOnDetailViewSource")
    @DisplayName("Start action visibility on Process detail view")
    void givenExistingActiveProcessDefinition_whenOpenDetailView_thenStartActionVisibilityMatchesPermissions(
            Class<?> roleClass,
            boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-process-start", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-start", "password");

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getStartProcessBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getGeneralPanel().getStartProcessBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("migrateVisibilityOnDetailViewSource")
    @DisplayName("Migrate action visibility on Process detail view")
    void givenExistingProcessDefinition_whenOpenDetailView_thenMigrateActionVisibilityMatchesPermissions(
            Class<?> roleClass,
            boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-process-migrate", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-migrate", "password");

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getMigrateBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getGeneralPanel().getMigrateBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("deleteVisibilityOnDetailViewSource")
    @DisplayName("Delete action visibility on Process detail view")
    void givenExistingProcessDefinition_whenOpenDetailView_thenDeleteActionVisibilityMatchesPermissions(
            Class<?> roleClass,
            boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-process-delete", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-delete", "password");

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getDeleteBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getGeneralPanel().getDeleteBtn().shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("viewDeploymentVisibilityOnDetailViewSource")
    @DisplayName("View deployment action visibility on Process detail view")
    void givenExistingProcessDefinition_whenOpenDetailView_thenViewDeploymentActionVisibilityMatchesPermissions(
            Class<?> roleClass,
            boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-process-deployment", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-process-deployment", "password");

        // when
        ProcessDefinitionDetailView detailView = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval");

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getPropertiesPanel().getViewDeploymentBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getGeneralPanel().getPropertiesPanel().getViewDeploymentBtn().shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Process detail view policy")
    void givenUserWithoutProcessDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        String processDefinitionId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn")
                .getDeployedProcessVersions("vacation_approval")
                .get(0);
        controlTestDataCreator.createUser(
                "test-user-process-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-process-no-detail-view-policy", "password");

        // when
        open("/bpm/process-definitions/" + processDefinitionId);

        // then
        $j(ProcessDefinitionDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/process-definitions/" + processDefinitionId + "'"));
    }

    private static Stream<Arguments> activateVisibilityOnDetailViewSource() {
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

    private static Stream<Arguments> suspendVisibilityOnDetailViewSource() {
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

    private static Stream<Arguments> startVisibilityOnDetailViewSource() {
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

    private static Stream<Arguments> migrateVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without migrate permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with migrate permission", TestProcessDefinitionMigratePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> deleteVisibilityOnDetailViewSource() {
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

    private static Stream<Arguments> viewDeploymentVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without deployment view permission", TestProcessDefinitionDetailAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with deployment view permission", TestProcessDefinitionDetailDeploymentViewRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
