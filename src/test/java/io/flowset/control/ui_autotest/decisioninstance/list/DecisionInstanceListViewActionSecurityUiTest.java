/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisioninstance.list;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceDecisionViewRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceListNoLinkedDetailViewRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceListAccessRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceProcessViewRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceProcessInstanceViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView;
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
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.DECISION_DEFINITION_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.DECISION_INSTANCE_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.PROCESS_DEFINITION_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.PROCESS_ID_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.PROCESS_INSTANCE_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceListView.PROCESS_INSTANCE_ID_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on decision instance list view")
@Tag("security")
public class DecisionInstanceListViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @DisplayName("Decision instance ID link availability on decision instance list view")
    void givenExistingDecisionInstance_whenOpenDecisionInstanceListView_thenDecisionInstanceIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-instance-list-id", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-instance-list-id", "password");

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        if (expectedEnabled) {
            listView.getRowByDecisionInstanceId(decisionInstanceId)
                    .getCellByIndex(DECISION_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByDecisionInstanceId(decisionInstanceId)
                    .getCellByIndex(DECISION_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("decisionLinkAvailabilitySource")
    @DisplayName("Decision link availability on decision instance list view")
    void givenExistingDecisionInstance_whenOpenDecisionInstanceListView_thenDecisionLinkButtonStateMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-instance-list-decision", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-instance-list-decision", "password");

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        if (expectedEnabled) {
            listView.getRowByDecisionInstanceId(decisionInstanceId)
                    .getCellByIndex(DECISION_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByDecisionInstanceId(decisionInstanceId)
                    .getCellByIndex(DECISION_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processInstanceLinkAvailabilitySource")
    @DisplayName("Process instance link availability on decision instance list view")
    void givenExistingDecisionInstance_whenOpenDecisionInstanceListView_thenProcessInstanceLinkButtonStateMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-instance-list-process-instance", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-instance-list-process-instance", "password");

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        if (expectedEnabled) {
            listView.getRowByDecisionInstanceId(decisionInstanceId)
                    .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_INSTANCE_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByDecisionInstanceId(decisionInstanceId)
                    .getCellByIndex(PROCESS_INSTANCE_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_INSTANCE_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processLinkVisibilitySource")
    @DisplayName("Process link visibility on decision instance list view")
    void givenExistingDecisionInstance_whenOpenDecisionInstanceListView_thenProcessDefinitionLinkButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-instance-list-process", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-instance-list-process", "password");

        // when
        DecisionInstanceListView listView = mainView.openDecisionInstanceListView();

        // then
        if (expectedVisible) {
            listView.getRowByDecisionInstanceId(decisionInstanceId)
                    .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByDecisionInstanceId(decisionInstanceId)
                    .getCellByIndex(PROCESS_DEFINITION_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(PROCESS_ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Decision instance list view policy")
    void givenUserWithoutDecisionInstanceListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-decision-instance-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-decision-instance-no-list-view-policy", "password");

        // when
        open("/bpm/decision-instances");

        // then
        $j(DecisionInstanceListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/decision-instances'"));
    }

    private static Stream<Arguments> decisionLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without decision view permission", TestDecisionInstanceListAccessRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without decision definition detail view access",
                                TestDecisionInstanceListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with decision view permission", TestDecisionInstanceDecisionViewRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> processInstanceLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process instance view permission", TestDecisionInstanceDecisionViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("without process instance detail view access",
                                TestDecisionInstanceListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with process instance view permission", TestDecisionInstanceProcessInstanceViewRole.class),
                        Named.of("enabled", true)
                )
        );
    }

    private static Stream<Arguments> processLinkVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process definition view permission", TestDecisionInstanceDecisionViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("without process definition detail view access",
                                TestDecisionInstanceListNoLinkedDetailViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with process definition view permission", TestDecisionInstanceProcessViewRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without decision instance detail view access",
                                TestDecisionInstanceListNoLinkedDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with decision instance detail view access",
                                TestDecisionInstanceListAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
