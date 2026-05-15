/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailDialog;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import io.flowset.control.test_support.ui.view.main.DashboardFragment;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.util.UUID;

import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.UiTestSupport.DATA_SAVING_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Actions related to BPM engine on dashboard")
public class DashboardEngineActionsUiTest extends AbstractUiTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("delete from CONTROL_BPM_ENGINE where NAME like 'dashboard-test-engine-%'");
    }

    @Test
    @DisplayName("Refresh action is visible if BPM engine exists")
    void givenExistingBpmEngine_whenOpenDashboard_thenRefreshActionEnabledRuleApplied() {
        // given
        String engineName = "dashboard-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, false);

        // when
        MainView mainView = loginAsAdmin();

        // then
        DashboardFragment dashboard = mainView.getDashboardFragment();
        dashboard.getRefreshBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Refresh action is hidden if no BPM engines exist")
    void givenNoEngines_whenOpenDashboard_thenRefreshActionHidden() {
        // when
        MainView mainView = loginAsAdmin();

        // then
        mainView.getDashboardFragment()
                .getRefreshBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Create engine action is hidden on Dashboard if BPM engine exists")
    void givenExistingBpmEngine_whenOpenDashboard_thenCreateEngineActionHidden() {
        // given
        String engineName = "dashboard-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, false);

        // when
        MainView mainView = loginAsAdmin();

        // then
        mainView.getDashboardFragment()
                .getCreateBpmEngineBtn()
                .shouldNotBe(VISIBLE);
    }

    @Test
    @DisplayName("Create engine action opens engine detail view dialog for new engine")
    void givenNoEngines_whenOpenDashboardAndClickAddEngine_thenBpmEngineDetailViewDialogOpened() {
        // when
        MainView mainView = loginAsAdmin();

        // then
        mainView.getDashboardFragment().getCreateBpmEngineBtn().click();

        $j(BpmEngineDetailDialog.class).exists()
                .displayed()
                .shouldHave(dialogHeader("New BPM engine"));
    }

    @Test
    @DisplayName("Create engine action: notification is shown after confirmation")
    void givenNoEngines_whenAddEngineAndConfirm_thenBpmEngineSelected() {
        // given
        String engineName = "dashboard-test-engine-" + System.currentTimeMillis();
        String engineBaseUrl = "http://%s.invalid/engine-rest".formatted(UUID.randomUUID());

        MainView mainView = loginAsAdmin();

        // when
        mainView.getDashboardFragment().getCreateBpmEngineBtn().click();

        BpmEngineDetailDialog dialog = $j(BpmEngineDetailDialog.class).exists().displayed();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class);
        detailView.getNameField().setValue(engineName);
        detailView.getBaseUrlField().setValue(engineBaseUrl);
        detailView.getSaveAndCloseButton().click();

        dialog.shouldNotBe(EXIST, Duration.ofSeconds(DATA_SAVING_WAIT_DURATION_SEC));

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTitle("The \"%s\" BPM engine selected".formatted(engineName)));
    }

    @Test
    @DisplayName("Create engine action: no engine is selected after cancellation")
    void givenNoEngines_whenAddEngineAndCancel_thenNoBpmEngineSelected() {
        // given
        String engineName = "dashboard-test-engine-" + System.currentTimeMillis();
        String engineBaseUrl = "http://%s.invalid/engine-rest".formatted(UUID.randomUUID());

        MainView mainView = loginAsAdmin();

        // when
        mainView.getDashboardFragment().getCreateBpmEngineBtn().click();

        $j(BpmEngineDetailDialog.class).exists().displayed();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class);
        detailView.getNameField().setValue(engineName);
        detailView.getBaseUrlField().setValue(engineBaseUrl);
        detailView.getCloseButton().click();

        // then
        mainView.getEngineStatusBadge()
                .getConnectionStatusText()
                .getDelegate()
                .shouldHave(text("No BPM engine"));

        mainView.getDashboardFragment()
                .getCreateBpmEngineBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }
}
