/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.deployment.upload;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDefinitionDeployPermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDeployPermissionRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailAccessRole;
import io.flowset.control.test_support.ui.view.deploymentupload.DeploymentUploadView;
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

@DisplayName("Secured actions availability on Upload deployment view")
@Tag("security")
public class DeploymentUploadViewActionSecurityUiTest extends AbstractUiTest {

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
    @MethodSource("deployButtonVisibilitySource")
    @DisplayName("Deploy button visibility on Upload deployment view")
    void givenLoggedInUser_whenOpenDeploymentUploadView_thenDeployButtonVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-deployment-upload", "password", roleClass);

        loginAs("test-user-deployment-upload", "password");

        // when
        open("/bpm/deployments/upload");
        DeploymentUploadView uploadView = $j(DeploymentUploadView.class)
                .exists()
                .displayed();

        // then
        if (expectedVisible) {
            uploadView.getOkBtn().shouldBe(VISIBLE);
        } else {
            uploadView.getOkBtn().shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Upload deployment view policy")
    void givenUserWithoutDeploymentUploadViewPolicy_whenOpenViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-no-deployment-upload-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-no-deployment-upload-view-policy", "password");

        // when
        open("/bpm/deployments/upload");

        // then
        $j(DeploymentUploadView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/deployments/upload'"));
    }

    private static Stream<Arguments> deployButtonVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without deploy permissions", TestProcessDefinitionDetailAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with process deploy permission", TestProcessDefinitionDeployPermissionRole.class),
                        Named.of("visible", true)
                ),
                Arguments.of(
                        Named.of("with decision deploy permission", TestDecisionDefinitionDeployPermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
