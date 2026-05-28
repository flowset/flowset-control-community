/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.deployment.list;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.deployment.TestDeploymentListNoDetailViewRole;
import io.flowset.control.test_support.security.role.deployment.TestDeploymentDeletePermissionRole;
import io.flowset.control.test_support.ui.component.GridContextMenu;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.deployment.DeploymentListView;
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
import static io.flowset.control.test_support.ui.view.deployment.DeploymentListView.DEPLOYMENT_ID_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.deployment.DeploymentListView.ID_BUTTON_BY;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Deployment list view")
@Tag("security")
public class DeploymentListViewActionSecurityUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    ControlTestDataCreator controlTestDataCreator;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

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
    @MethodSource("bulkRemoveVisibilityOnListViewSource")
    @DisplayName("Bulk Remove action visibility")
    void givenExistingDeployment_whenOpenDeploymentList_thenBulkRemoveActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");

        controlTestDataCreator.createUser("test-user-deployment-bulk-remove", "password", roleClass);

        MainView mainView = loginAs("test-user-deployment-bulk-remove", "password");

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        GridContextMenu contextMenu = listView.openDeploymentsGridContextMenu();
        if (expectedVisible) {
            listView.getBulkRemoveBtn().shouldBe(VISIBLE);
            contextMenu.find(text("Remove")).shouldBe(VISIBLE);
        } else {
            listView.getBulkRemoveBtn().shouldNotBe(VISIBLE);
            contextMenu.find(text("Remove")).shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilityOnListViewSource")
    @DisplayName("Deployment ID link availability")
    void givenExistingDeployment_whenOpenDeploymentList_thenDeploymentIdLinkAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedAvailable
    ) {
        // given
        String deploymentId = camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn")
                .getId();

        controlTestDataCreator.createUser("test-user-deployment-detail-link", "password", roleClass);

        MainView mainView = loginAs("test-user-deployment-detail-link", "password");

        // when
        DeploymentListView listView = mainView.openDeploymentListView();

        // then
        if (expectedAvailable) {
            listView.waitUntilDataLoading()
                    .getRowByDeploymentId(deploymentId)
                    .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.waitUntilDataLoading()
                    .getRowByDeploymentId(deploymentId)
                    .getCellByIndex(DEPLOYMENT_ID_COLUMN_INDEX)
                    .getCellContent()
                    .find(ID_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Deployment list view policy")
    void givenUserWithoutDeploymentListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-deployment-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-deployment-no-list-view-policy", "password");

        // when
        open("/bpm/deployments");

        // then
        $j(DeploymentListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/deployments'"));
    }

    private static Stream<Arguments> bulkRemoveVisibilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without delete permission", TestUiPermissionFullAccessReadRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with delete permission", TestDeploymentDeletePermissionRole.class),
                        Named.of("visible", true)
                )
        );
    }

    private static Stream<Arguments> detailLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without deployment detail view access", TestDeploymentListNoDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with deployment detail view access", TestUiPermissionFullAccessReadRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
