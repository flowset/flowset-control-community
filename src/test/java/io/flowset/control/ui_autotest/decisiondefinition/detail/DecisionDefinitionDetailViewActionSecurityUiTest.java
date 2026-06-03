/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.detail;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDefinitionDetailAccessRole;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDefinitionDetailDeploymentViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.detail.DecisionDefinitionDetailView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Decision definition detail view")
@Tag("security")
public class DecisionDefinitionDetailViewActionSecurityUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

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
    @MethodSource("viewDeploymentVisibilitySource")
    @DisplayName("View deployment button visibility")
    void givenExistingDecisionDefinition_whenOpenDetailView_thenViewDeploymentButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn");

        controlTestDataCreator.createUser("test-user-decision-definition-deployment", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-definition-deployment", "password");

        // when
        DecisionDefinitionDetailView detailView = mainView.openDecisionDefinitionListView()
                .openDetailViewByKey("decision_testDmn");

        detailView.getGeneralPanel().getInfoBtn().click();

        // then
        if (expectedVisible) {
            detailView.getGeneralPanel().getViewDeploymentBtn().shouldBe(VISIBLE).shouldBe(ENABLED);
        } else {
            detailView.getGeneralPanel().getViewDeploymentBtn().shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Decision definition detail view policy")
    void givenUserWithoutDecisionDefinitionDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        String decisionDefinitionId = camundaRestTestHelper.createDeployment(camunda7, "test_support/dmn/testDmn.dmn")
                .getDeployedDecisionDefinitions()
                .keySet()
                .iterator()
                .next();
        controlTestDataCreator.createUser(
                "test-user-decision-definition-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-decision-definition-no-detail-view-policy", "password");

        // when
        open("/bpm/decision-definitions/" + decisionDefinitionId);

        // then
        $j(DecisionDefinitionDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/decision-definitions/" + decisionDefinitionId + "'"));
    }

    private static Stream<Arguments> viewDeploymentVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without deployment view permission", TestDecisionDefinitionDetailAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with deployment view permission", TestDecisionDefinitionDetailDeploymentViewRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
