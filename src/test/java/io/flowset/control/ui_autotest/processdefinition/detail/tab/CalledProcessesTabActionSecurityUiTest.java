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
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.processdefinition.detail.CalledProcessesTabFragment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.flowset.control.test_support.ui.view.processdefinition.detail.CalledProcessesTabFragment.CALLED_ELEMENT_COLUMN_INDEX;
import static io.flowset.control.test_support.ui.view.processdefinition.detail.CalledProcessesTabFragment.KEY_BUTTON_BY;
import static io.flowset.control.test_support.ui.view.processdefinition.detail.CalledProcessesTabFragment.PREVIEW_BUTTON_BY;
import static com.codeborne.selenide.Condition.interactable;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;

@WithRunningExternalEngine
@DisplayName("Secured actions availability on Called processes tab")
@Tag("security")
public class CalledProcessesTabActionSecurityUiTest extends AbstractCamunda7UiTest {

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

    @Test
    @DisplayName("Called process view action visibility")
    void givenExistingCalledProcess_whenOpenCalledProcessesTab_thenKeyButtonVisibleAndInteractable() {
        // given
        controlTestDataCreator.createUser(
                "test-user-called-processes",
                "password",
                TestProcessDefinitionDetailAccessRole.class
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn");

        MainView mainView = loginAs("test-user-called-processes", "password");

        // when
        CalledProcessesTabFragment calledProcessesTab = mainView.openProcessListView()
                .openDetailViewByKey("testSkipSubprocessMain")
                .openCalledProcessesTab();

        // then
        calledProcessesTab.getRowByCalledElement("testSkipSubprocess")
                .getCellByIndex(CALLED_ELEMENT_COLUMN_INDEX)
                .getCellContent()
                .find(KEY_BUTTON_BY)
                .shouldBe(interactable)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Called process preview action visibility")
    void givenExistingCalledProcess_whenOpenCalledProcessesTab_thenPreviewButtonVisibleAndInteractable() {
        // given
        controlTestDataCreator.createUser(
                "test-user-called-processes",
                "password",
                TestProcessDefinitionDetailAccessRole.class
        );

        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/terminateinstance/testSkipSubprocess.bpmn")
                .deploy("test_support/terminateinstance/testSkipSubprocessMain.bpmn");

        MainView mainView = loginAs("test-user-called-processes", "password");

        // when
        CalledProcessesTabFragment calledProcessesTab = mainView.openProcessListView()
                .openDetailViewByKey("testSkipSubprocessMain")
                .openCalledProcessesTab();

        // then
        calledProcessesTab.getRowByCalledElement("testSkipSubprocess")
                .getCellByIndex(CALLED_ELEMENT_COLUMN_INDEX)
                .getCellContent()
                .find(PREVIEW_BUTTON_BY)
                .shouldBe(interactable)
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }
}
