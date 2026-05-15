/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.component.JmixDialog;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailDialog;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import io.flowset.control.test_support.ui.view.engine.EngineConnectionSettingsView;
import io.flowset.control.test_support.ui.view.main.EngineStatusBadgeFragment;
import io.flowset.control.test_support.ui.view.main.SelectEnginePopoverContent;
import io.jmix.masquerade.component.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.codeborne.selenide.Selenide.$;
import static io.flowset.control.test_support.ui.view.main.EngineStatusBadgeFragment.POPOVER_CONTENT_BY;
import static io.jmix.masquerade.JConditions.*;
import static io.jmix.masquerade.Masquerade.$j;
import static org.openqa.selenium.By.cssSelector;

@DisplayName("Actions on Engine status badge")
public class EngineStatusBadgeActionsUiTest extends AbstractUiTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("delete from CONTROL_BPM_ENGINE where NAME like '%test-engine%'");
    }

    @Test
    @DisplayName("View action updates engine popover visibility")
    void givenLoggedInUser_whenOnMainView_thenViewEngineConfigActionEnabledRuleApplied() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        EngineStatusBadgeFragment badge = mainView.getEngineStatusBadge();
        badge.getViewEngineConfigBtn().click();

        // then
        $(POPOVER_CONTENT_BY)
                .shouldBe(EXIST)
                .shouldBe(VISIBLE);

        badge.getViewEngineConfigBtn().click();

        $(POPOVER_CONTENT_BY)
                .shouldNotBe(EXIST)
                .shouldNotBe(VISIBLE);
    }


    @Test
    @DisplayName("Add engine action opens BPM engine detail view dialog for new engine")
    void givenLoggedInUser_whenClickAddBpmEngineInEnginePopover_thenBpmEngineDetailViewDialogOpened() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.getEngineStatusBadge()
                .openSelectEnginePopover()
                .getCreateBpmEngineBtn()
                .click();

        // then
        $j(BpmEngineDetailDialog.class).exists()
                .displayed()
                .shouldHave(dialogHeader("New BPM engine"));
    }

    @Test
    @DisplayName("Add engine action: engine selected notification is shown after confirmation")
    void givenNoEngines_whenAddEngineAndConfirm_thenBpmEngineSelected() {
        // given
        String engineName = "badge-test-engine-" + System.currentTimeMillis();
        String url = "http://%s:8080/engine-rest".formatted(engineName);
        MainView mainView = loginAsAdmin();

        // when
        mainView.getEngineStatusBadge()
                .openSelectEnginePopover()
                .getCreateBpmEngineBtn()
                .click();

        $j(BpmEngineDetailDialog.class).exists().displayed();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class);
        detailView.getNameField().setValue(engineName);
        detailView.getBaseUrlField().setValue(url);
        detailView.getSaveAndCloseButton().click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTitle("The \"%s\" BPM engine selected".formatted(engineName)));
    }

    @Test
    @DisplayName("Add engine action: no engine is selected after cancellation")
    void givenNoEngines_whenAddEngineAndCancel_thenNoBpmEngineSelected() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        mainView.getEngineStatusBadge()
                .openSelectEnginePopover()
                .getCreateBpmEngineBtn()
                .click();

        $j(BpmEngineDetailDialog.class).exists().displayed();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class);
        detailView.getNameField().setValue("Test engine");
        detailView.getBaseUrlField().setValue("http://localhost:8080/engine-rest");
        detailView.getCloseButton().click();

        $j(JmixDialog.class, cssSelector("[aria-label='You have unsaved changes']"))
                .shouldBe(VISIBLE)
                .getCancelBtn()
                .click();

        // then
        $j(Notification.class).shouldNotBe(VISIBLE);

        mainView.getDashboardFragment()
                .getCreateBpmEngineBtn()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED);
    }

    @Test
    @DisplayName("Selecting engine in popover: notification is shown after selection")
    void givenMultipleBpmEngines_whenSelectEngineInEnginePopover_thenSelectedEngineChanged() {
        // given
        String defaultEngineName = "default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(defaultEngineName, true);

        String nonDefaultEngineName = "non-default-test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(nonDefaultEngineName, false);

        MainView mainView = loginAsAdmin();

        // when
        SelectEnginePopoverContent popover = mainView.getEngineStatusBadge().openSelectEnginePopover();

        popover.getEngineListBox()
                .findItemByEngineName(nonDefaultEngineName)
                .shouldBe(VISIBLE)
                .getDelegate()
                .click();

        // then
        $j(Notification.class)
                .shouldBe(VISIBLE)
                .shouldHave(notificationTitle("The \"%s\" BPM engine selected".formatted(nonDefaultEngineName)));
    }

    @Test
    @DisplayName("Advanced mode action opens engine connections settings view")
    void givenLoggedInUser_whenOpenSelectEnginePopover_thenAdvancedModeActionEnabledRuleApplied() {
        // given
        MainView mainView = loginAsAdmin();

        // when
        SelectEnginePopoverContent popover = mainView.getEngineStatusBadge().openSelectEnginePopover();
        popover.getAdvancedModeBtn().click();

        // then
        $j(EngineConnectionSettingsView.class)
                .exists()
                .displayed();
    }
}
