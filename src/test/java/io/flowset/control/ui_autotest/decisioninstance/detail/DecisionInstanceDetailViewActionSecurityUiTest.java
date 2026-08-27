/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisioninstance.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceDecisionViewRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceListAccessRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceProcessViewRole;
import io.flowset.control.test_support.security.role.decisioninstance.TestDecisionInstanceProcessInstanceViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisioninstance.DecisionInstanceDetailView;
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
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on decision instance detail view")
@Tag("security")
public class DecisionInstanceDetailViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("decisionDefinitionButtonVisibilityOnDetailViewSource")
    @DisplayName("Decision definition navigation button visibility")
    void givenExistingDecisionInstance_whenOpenDetailView_thenDecisionDefinitionButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-instance-detail-decision", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-instance-detail-decision", "password");

        // when
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);
        detailView.getInfoBtn().click();

        // then
        if (expectedVisible) {
            detailView.getOpenDecisionDefinitionEditorBtn()
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            detailView.getOpenDecisionDefinitionEditorBtn()
                    .shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processInstanceButtonVisibilityOnDetailViewSource")
    @DisplayName("Process instance navigation button visibility")
    void givenExistingDecisionInstance_whenOpenDetailView_thenProcessInstanceButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-instance-detail-process-instance", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-instance-detail-process-instance", "password");

        // when
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);
        detailView.getInfoBtn().click();

        // then
        if (expectedVisible) {
            detailView.getOpenProcessInstanceEditorBtn()
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            detailView.getOpenProcessInstanceEditorBtn()
                    .shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("processDefinitionButtonVisibilityOnDetailViewSource")
    @DisplayName("Process definition navigation button visibility")
    void givenExistingDecisionInstance_whenOpenDetailView_thenProcessDefinitionButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);

        controlTestDataCreator.createUser("test-user-decision-instance-detail-process", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-instance-detail-process", "password");

        // when
        DecisionInstanceDetailView detailView = mainView.openDecisionInstanceListView()
                .openDetailViewByDecisionInstanceId(decisionInstanceId);
        detailView.getInfoBtn().click();

        // then
        if (expectedVisible) {
            detailView.getOpenProcessDefinitionEditorBtn()
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            detailView.getOpenProcessDefinitionEditorBtn()
                    .shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Decision instance detail view policy")
    void givenUserWithoutDecisionInstanceDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        String decisionInstanceId = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn")
                .startByKey("testProcessWithDecision")
                .getDecisionInstances("decision_testDmn")
                .get(0);
        controlTestDataCreator.createUser(
                "test-user-decision-instance-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-decision-instance-no-detail-view-policy", "password");

        // when
        open("/bpm/decision-instances/" + decisionInstanceId);

        // then
        $j(DecisionInstanceDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/decision-instances/" + decisionInstanceId + "'"));
    }

    private static Stream<Arguments> processInstanceButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process instance view permission", TestDecisionInstanceDecisionViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with process instance view permission", TestDecisionInstanceProcessInstanceViewRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> decisionDefinitionButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without decision definition view permission", TestDecisionInstanceListAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with decision definition view permission", TestDecisionInstanceDecisionViewRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> processDefinitionButtonVisibilityOnDetailViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without process definition view permission", TestDecisionInstanceDecisionViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with process definition view permission", TestDecisionInstanceProcessViewRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
