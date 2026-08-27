/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.processdefinition.detail.tab;

import io.flowset.control.test_support.ControlTestDataCreator;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailAccessRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailDecisionViewRole;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionDetailNoDecisionDetailViewRole;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.CalledDecisionsTabFragment;
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

import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.flowset.control.test_support.ui.UiTestSupport.waitUntilPageLoading;
import static io.flowset.control.test_support.ui.view.processdefinition.detail.CalledDecisionsTabFragment.*;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Decisions tab")
@Tag("security")
public class DecisionsTabActionSecurityUiTest extends AbstractCamunda7UiTest {

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
    @MethodSource("decisionVisibilitySource")
    @DisplayName("Decision view action visibility")
    void givenExistingCalledDecision_whenOpenDetailView_thenDecisionViewActionVisibilityMatchesPermissions(
            Class<?> roleClass, boolean expectedEnabled
    ) {
        // given
        controlTestDataCreator.createUser("test-user-called-decisions", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn");

        MainView mainView = loginAs("test-user-called-decisions", "password");

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .openDecisionsTab();

        // then
        if (expectedEnabled) {
            decisionsTab.getRowByDecisionRef("decision_testDmn")
                    .getCellByIndex(DECISION_REF_COLUMN_INDEX)
                    .getCellContent()
                    .find(KEY_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            decisionsTab.getRowByDecisionRef("decision_testDmn")
                    .getCellByIndex(DECISION_REF_COLUMN_INDEX)
                    .getCellContent()
                    .find(KEY_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldNotBe(ENABLED);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("decisionVisibilitySource")
    @DisplayName("Decision preview action visibility")
    void givenExistingCalledDecision_whenOpenDetailView_thenDecisionPreviewActionVisibilityMatchesPermissions(
            Class<?> roleClass,
            boolean expectedVisible
    ) {
        // given
        controlTestDataCreator.createUser("test-user-called-decisions", "password", roleClass);

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn");

        MainView mainView = loginAs("test-user-called-decisions", "password");

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .openDecisionsTab();

        // then
        if (expectedVisible) {
            decisionsTab.getRowByDecisionRef("decision_testDmn")
                    .getCellByIndex(DECISION_REF_COLUMN_INDEX)
                    .getCellContent()
                    .find(PREVIEW_BUTTON_BY)
                    .shouldBe(VISIBLE)
                    .shouldBe(ENABLED);
        } else {
            decisionsTab.getRowByDecisionRef("decision_testDmn")
                    .getCellByIndex(DECISION_REF_COLUMN_INDEX)
                    .getCellContent()
                    .find(PREVIEW_BUTTON_BY)
                    .shouldNotBe(VISIBLE);
        }
    }

    @Test
    @DisplayName("Row double-click: decision detail view not opened without view permission")
    void givenExistingCalledDecisionAndNoViewPermission_whenDoubleClickRow_thenDecisionDetailViewNotOpened() {
        // given
        controlTestDataCreator.createUser("test-user-called-decisions", "password",
                TestProcessDefinitionDetailAccessRole.class);

       applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/dmn/testDmn.dmn")
                .deploy("test_support/testProcessWithDecision.bpmn");

        MainView mainView = loginAs("test-user-called-decisions", "password");

        // when
        CalledDecisionsTabFragment decisionsTab = mainView.openProcessListView()
                .openDetailViewByKey("testProcessWithDecision")
                .openDecisionsTab();

        decisionsTab.getRowByDecisionRef("decision_testDmn")
                .getCellByIndex(BINDING_COLUMN_INDEX)
                .getCellContent()
                .doubleClick();

        waitUntilPageLoading();

        // then
        webdriver().shouldHave(urlContaining("/bpm/process-definitions/"));
    }

    private static Stream<Arguments> decisionVisibilitySource() {
        return Stream.of(
                Arguments.of(
                        Named.of("without decision view permission", TestProcessDefinitionDetailAccessRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("without decision detail view access",
                                TestProcessDefinitionDetailNoDecisionDetailViewRole.class),
                        Named.of("hidden", false)
                ),
                Arguments.of(
                        Named.of("with decision view permission", TestProcessDefinitionDetailDecisionViewRole.class),
                        Named.of("visible", true)
                )
        );
    }
}
