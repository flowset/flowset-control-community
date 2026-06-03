/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.detail.tab;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDefinitionDetailNoLinkedDetailViewRole;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDetailDecisionInstanceViewRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceProcessInstanceViewRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceProcessViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionInstancesTabFragment;
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

import static io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionInstancesTabFragment.*;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Decision instances tab")
@Tag("security")
public class DecisionInstancesTabActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @DisplayName("Decision instance link availability with permission")
    void givenExistingDecisionInstance_whenOpenDecisionInstancesTab_thenDecisionInstanceLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser(
                "test-user-decision-detail-tab-decision-instance",
                "password",
                roleClass
        );

        MainView mainView = loginAs("test-user-decision-detail-tab-decision-instance", "password");

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        // then
        if (expectedAvailable) {
            tab.getRowByInstanceId(decisionInstanceId)
                    .getCellByIndex(DECISION_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            tab.getRowByInstanceId(decisionInstanceId)
                    .getCellByIndex(DECISION_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processInstanceLinkAvailabilitySource")
    @DisplayName("Process instance link availability")
    void givenExistingDecisionInstance_whenOpenDecisionInstancesTab_thenProcessInstanceLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-detail-tab-process-instance", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-detail-tab-process-instance", "password");

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        // then
        if (expectedAvailable) {
            tab.getRowByInstanceId(decisionInstanceId)
                    .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_INSTANCE_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            tab.getRowByInstanceId(decisionInstanceId)
                    .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_INSTANCE_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processLinkAvailabilitySource")
    @DisplayName("Process link visibility")
    void givenExistingDecisionInstance_whenOpenDecisionInstancesTab_thenProcessLinkVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-detail-tab-process", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-detail-tab-process", "password");

        // when
        DecisionInstancesTabFragment tab = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn")
                .openDecisionInstancesTab();

        // then
        if (expectedEnabled) {
            tab.getRowByInstanceId(decisionInstanceId)
                    .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            tab.getRowByInstanceId(decisionInstanceId)
                    .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    private static Stream<Arguments> processInstanceLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process instance view permission",
                                TestDecisionDetailDecisionInstanceViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without process instance detail view access",
                                TestDecisionDefinitionDetailNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with process instance view permission",
                                TestDecisionInstanceProcessInstanceViewRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> processLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process view permission", TestDecisionDetailDecisionInstanceViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without process definition detail view access",
                                TestDecisionDefinitionDetailNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with process view permission",
                                TestDecisionInstanceProcessViewRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without decision instance detail view access",
                                TestDecisionDefinitionDetailNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with decision instance detail view access",
                                TestDecisionDetailDecisionInstanceViewRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
