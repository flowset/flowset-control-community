/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailAccessRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailDecisionViewRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailNoStatisticsRole;
import io.flowset.control.test_support.ui.component.BpmnViewerFragment;
import io.flowset.control.test_support.ui.view.MainView;
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

import static com.codeborne.selenide.Condition.visible;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions visibility on diagram in Process detail view")
@Tag("security")
public class ProcessDefinitionDiagramSecurityUiTest extends AbstractCamunda7UiTest {

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

    @Test
    @DisplayName("Called process overlay visibility on Process detail view diagram")
    void givenExistingProcessWithCallActivity_whenOpenDetailView_thenCalledProcessOverlayVisible() {
        // given
        controlTestDataCreator.createUser("test-user-diagram", "password",
                TestProcessDefinitionDetailAccessRole.class);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testFailedJobIncident.bpmn")
                .deploy("test_support/testPropagatedJobIncident.bpmn");

        MainView mainView = loginAs("test-user-diagram", "password");

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("testPropagatedJobIncident")
                .getBpmnViewerFragment();

        // then
        viewerFragment.getCalledProcessOverlay("failedSubprocessTask")
                .shouldBe(visible);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("decisionOverlayVisibilitySource")
    @DisplayName("Decision overlay visibility on Process detail view diagram")
    void givenExistingProcessWithDecision_whenOpenDetailView_thenDecisionOverlayVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-diagram", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn");

        MainView mainView = loginAs("test-user-diagram", "password");

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .getBpmnViewerFragment();

        // then
        if (expectedVisible) {
            viewerFragment.getDecisionOverlay("evaluateDecisionTask")
                    .shouldBe(visible);
        } else {
            viewerFragment.getDecisionOverlay("evaluateDecisionTask")
                    .shouldNotBe(visible);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("statisticsButtonVisibilitySource")
    @DisplayName("Activity statistics button visibility on Process detail view diagram")
    void givenExistingProcess_whenOpenDetailView_thenStatisticsButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-diagram", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        MainView mainView = loginAs("test-user-diagram", "password");

        // when
        BpmnViewerFragment viewerFragment = mainView.openProcessListView()
                .openDetailViewByKey("vacation_approval")
                .getBpmnViewerFragment();

        // then
        if (expectedVisible) {
            viewerFragment.getViewActivityStatisticsButton()
                    .shouldBe(VISIBLE);
        } else {
            viewerFragment.getViewActivityStatisticsButton()
                    .shouldNotBe(VISIBLE);
        }
    }

    private static Stream<Arguments> decisionOverlayVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without DecisionDefinitionData view permission", TestProcessDefinitionDetailAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with DecisionDefinitionData view permission", TestProcessDefinitionDetailDecisionViewRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> statisticsButtonVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without ProcessActivityStatistics view permission", TestProcessDefinitionDetailNoStatisticsRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with ProcessActivityStatistics view permission", TestProcessDefinitionDetailAccessRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
