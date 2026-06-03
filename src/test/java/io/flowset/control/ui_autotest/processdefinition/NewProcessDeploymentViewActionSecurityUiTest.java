/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDeployPermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailAccessRole;
import io.flowset.control.test_support.ui.view.processdefinition.NewProcessDeploymentView;
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

@DisplayName("Secured actions availability on Process deployment view")
@Tag("security")
public class NewProcessDeploymentViewActionSecurityUiTest extends AbstractUiTest {

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDownUsers() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME like 'test-user%'");
        jdbcTemplate.update(
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in (select ID from SEC_RESOURCE_ROLE " +
                        "where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("okButtonVisibilitySource")
    @DisplayName("OK button visibility on Process deployment view")
    void givenLoggedInUser_whenOpenNewProcessDeploymentView_thenOkButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-process-deployment-ok", "password", roleClass);

        loginAs("test-user-process-deployment-ok", "password");

        // when
        open("/bpm/new-process-deployment");
        NewProcessDeploymentView deploymentView = $j(NewProcessDeploymentView.class)
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
    @DisplayName("Direct URL navigation is denied without Process deployment view policy")
    void givenUserWithoutProcessDeploymentViewPolicy_whenOpenViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-no-process-deployment-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-no-process-deployment-view-policy", "password");

        // when
        open("/bpm/new-process-deployment");

        // then
        $j(NewProcessDeploymentView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/new-process-deployment'"));
    }

    private static Stream<Arguments> okButtonVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without deploy permission", TestProcessDefinitionDetailAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with deploy permission", TestProcessDefinitionDeployPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
