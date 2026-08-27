/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine;

import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineListView;
import io.flowset.control.test_support.ui.view.engine.EngineConnectionSettingsView;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.UiTestSupport.DATA_SAVING_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.JSelectors.byUiTestId;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Actions on Engine connection settings view")
@WithRunningExternalEngine(save = false)
public class EngineConnectionSettingsViewActionsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("delete from CONTROL_BPM_ENGINE where NAME like '%test-engine%'");
    }

    @Test
    @DisplayName("Ok action: notification about selected engine is shown")
    void givenMultipleBpmEngines_whenOpenConnectionSettingsAndSelectEngine_thenEngineChanged() {
        // given
        String defaultEngineName = "default-test-engine-" + System.currentTimeMillis();
        String nonDefaultEngineName = "non-default-test-engine-" + System.currentTimeMillis();

        controlTestDataCreator.createRandomBpmEngine(defaultEngineName, true);
        controlTestDataCreator.createRandomBpmEngine(nonDefaultEngineName, false);

        MainView mainView = loginAsAdmin();

        // when
        EngineConnectionSettingsView view = mainView.openEngineConnectionView();
        view.getBpmEnginesComboBox()
                .clickItemsOverlay()
                .getVisibleElements()
                .find(text(nonDefaultEngineName))
                .shouldBe(VISIBLE)
                .click();

        view.getUpdateEngineBtn().click();
        view.shouldNotBe(VISIBLE, Duration.ofSeconds(DATA_SAVING_WAIT_DURATION_SEC));

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTitle("The \"%s\" BPM engine selected".formatted(nonDefaultEngineName)));
    }

    @Test
    @DisplayName("Cancel action does not change selected BPM engine")
    void givenMultipleBpmEngines_whenOpenConnectionSettingsAndClick_thenEngineNotChanged() {
        // given
        String defaultEngineName = "default-test-engine-" + System.currentTimeMillis();
        String nonDefaultEngineName = "non-default-test-engine-" + System.currentTimeMillis();

        controlTestDataCreator.createRandomBpmEngine(defaultEngineName, true);
        controlTestDataCreator.createRandomBpmEngine(nonDefaultEngineName, false);

        MainView mainView = loginAsAdmin();

        // when
        EngineConnectionSettingsView view = mainView.openEngineConnectionView();
        view.getBpmEnginesComboBox()
                .clickItemsOverlay()
                .getVisibleElements()
                .find(text(nonDefaultEngineName))
                .shouldBe(VISIBLE).click();

        view.getCloseBtn().click();

        // then
        $j(Notification.class)
                .shouldNotBe(EXIST);
    }

    @Test
    @DisplayName("Test connection action availability on Engine connection settings view")
    void givenSelectedBpmEngine_whenOpenEngineConnectionSettings_thenTestConnectionActionEnabledRuleApplied() {
        // given
        String defaultEngineName = "test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(defaultEngineName, true);

        MainView mainView = loginAsAdmin();

        // when
        EngineConnectionSettingsView view = mainView.openEngineConnectionView();

        // then
        view.getTestConnectionBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);

        view.getBpmEnginesComboBox()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .setValue("");

        view.getTestConnectionBtn()
                .shouldBe(VISIBLE)
                .shouldNotBe(ENABLED);
    }

    @Test
    @DisplayName("Entity lookup action on engines combobox of BPM engine lookup")
    void givenSelectedBpmEngine_whenOpenEngineConnectionSettings_thenEntityLookupActionOpensLookupDialog() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        EngineConnectionSettingsView view = mainView.openEngineConnectionView();
        view.getBpmEnginesComboBox()
                .getDelegate()
                .find(byUiTestId("entityLookup")).click();

        // then
        $j(BpmEngineListView.class).exists()
                .displayed();
    }
}
