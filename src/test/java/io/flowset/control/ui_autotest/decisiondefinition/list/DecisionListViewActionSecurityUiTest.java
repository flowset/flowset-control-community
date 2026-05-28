/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.decisiondefinition.list;

import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDefinitionDetailAccessRole;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDefinitionDeployPermissionRole;
import io.flowset.control.test_support.security.role.decisiondefinition.TestDecisionDefinitionListNoDetailViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView;
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
import static io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView.KEY_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView.KEY_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView.NAME_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.decisiondefinition.DecisionDefinitionListView.NAME_COLUMN_INDEX;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Decision definition list view")
@Tag("security")
public class DecisionListViewActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("deployVisibilityOnListViewSource")
    @DisplayName("Deploy action visibility")
    void givenExistingDecisionDefinition_whenOpenDecisionDefinitionList_thenDeployActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedVisible
    ) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");

        controlTestDataCreator.createUser("test-user-decision-definition-deploy", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-definition-deploy", "password");

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();

        // then
        if (expectedVisible) {
            listView.getDeployBtn().shouldBe(VISIBLE);
            listView.openDecisionGridContextActions()
                    .find(text("Deploy"))
                    .shouldBe(VISIBLE);
        } else {
            listView.getDeployBtn().shouldNotBe(VISIBLE);
            listView.openDecisionGridContextActions()
                    .find(text("Deploy"))
                    .shouldNotBe(VISIBLE);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("detailLinkAvailabilityOnListViewSource")
    @DisplayName("Name and key links availability")
    void givenExistingDecisionDefinition_whenOpenDecisionDefinitionList_thenDetailLinksAvailabilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn");

        controlTestDataCreator.createUser("test-user-decision-definition-detail-link", "password", roleClass);

        MainView mainView = loginAs("test-user-decision-definition-detail-link", "password");

        // when
        DecisionDefinitionListView listView = mainView.openDecisionDefinitionListView();

        // then
        if (expectedEnabled) {
            listView.getRowByDecisionKey("decision_testDmn")
                    .getCellByIndex(NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
            listView.getRowByDecisionKey("decision_testDmn")
                    .getCellByIndex(KEY_COLUMN_INDEX)
                    .getCellContent()
                    .find(KEY_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            listView.getRowByDecisionKey("decision_testDmn")
                    .getCellByIndex(NAME_COLUMN_INDEX)
                    .getCellContent()
                    .find(NAME_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
            listView.getRowByDecisionKey("decision_testDmn")
                    .getCellByIndex(KEY_COLUMN_INDEX)
                    .getCellContent()
                    .find(KEY_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @Test
    @DisplayName("Direct URL navigation is denied without Decision definition list view policy")
    void givenUserWithoutDecisionDefinitionListViewPolicy_whenOpenListViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-decision-definition-no-list-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-decision-definition-no-list-view-policy", "password");

        // when
        open("/bpm/decision-definitions");

        // then
        $j(DecisionDefinitionListView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/decision-definitions'"));
    }

    private static Stream<Arguments> deployVisibilityOnListViewSource() {
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

    private static Stream<Arguments> detailLinkAvailabilityOnListViewSource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without decision definition detail view access",
                                TestDecisionDefinitionListNoDetailViewRole.class),
                        Named.of("disabled", false)
                ),
                Arguments.of(
                        Named.of("with decision definition detail view access",
                                TestDecisionDefinitionDetailAccessRole.class),
                        Named.of("enabled", true)
                )
        );
    }
}
