/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDefinitionDeployPermissionRole;
import io.flowset.control.test_support.ui.view.decisiondeployment.DecisionDeploymentView;
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
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Secured actions availability on Decision deployment view")
@Tag("security")
public class DecisionDeploymentViewActionSecurityUiTest extends AbstractUiTest {

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
    @MethodSource("okButtonVisibilitySource")
    @DisplayName("OK button visibility")
    void givenLoggedInUser_whenOpenDecisionDeploymentView_thenOkButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-decision-deployment-ok", "password", roleClass);

        loginAs("test-user-decision-deployment-ok", "password");

        // when
        open("/bpm/decision-deployment");
        DecisionDeploymentView deploymentView = $j(DecisionDeploymentView.class)
                .exists()
                .displayed();

        // then
        if (expectedVisible) {
            deploymentView.getOkBtn().shouldBe(VISIBLE);
        } else {
            deploymentView.getOkBtn().shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Decision deployment view policy")
    void givenUserWithoutDecisionDeploymentViewPolicy_whenOpenViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-no-decision-deployment-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-no-decision-deployment-view-policy", "password");

        // when
        open("/bpm/decision-deployment");

        // then
        $j(DecisionDeploymentView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/decision-deployment'"));
    }

    private static Stream<Arguments> okButtonVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without deploy permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with deploy permission", TestDecisionDefinitionDeployPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
