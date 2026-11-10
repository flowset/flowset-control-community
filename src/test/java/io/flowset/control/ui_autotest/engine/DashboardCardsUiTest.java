/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine;

import io.flowset.control.test_support.EnabledOnNoAuthentication;
import io.flowset.control.test_support.camunda7.AbstractCamunda7UiTest;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.engine.external.RunningExternalEngine;
import io.flowset.control.test_support.engine.external.WithRunningExternalEngine;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailDialog;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import io.flowset.control.test_support.ui.view.main.DashboardFragment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;

import static io.flowset.control.test_support.ui.UiTestSupport.DATA_SAVING_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Dashboard cards")
@WithRunningExternalEngine(save = false)
public class DashboardCardsUiTest extends AbstractCamunda7UiTest {

    @RunningExternalEngine
    ExternalEngine camunda7;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("delete from CONTROL_BPM_ENGINE where NAME like 'dashboard-test-engine-%'");
    }

    @EnabledOnNoAuthentication
    @Test
    @DisplayName("Create engine action: all cards shown for Camunda 7 engine after confirmation")
    void givenNoEngines_whenAddCamunda7EngineAndConfirm_thenAllCardsVisible() {
        // given
        String engineName = "dashboard-test-engine-" + System.currentTimeMillis();
        MainView mainView = loginAsAdmin();

        // when
        mainView.getDashboardFragment().getCreateBpmEngineBtn().click();

        BpmEngineDetailDialog dialog = $j(BpmEngineDetailDialog.class).exists().displayed();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class);
        detailView.getNameField().setValue(engineName);
        detailView.getTypeField().setValue("Camunda 7");
        detailView.getBaseUrlField().setValue(camunda7.getRestBaseUrl());
        detailView.getSaveAndCloseButton().click();

        dialog.shouldNotBe(EXIST, Duration.ofSeconds(DATA_SAVING_WAIT_DURATION_SEC));

        // then
        DashboardFragment dashboard = mainView.getDashboardFragment();
        dashboard.getNoEnginesContainer().shouldNotBe(VISIBLE);
        dashboard.getDashboardContainer().shouldBe(VISIBLE);
        dashboard.getDeployedProcessesCard().shouldBe(VISIBLE);
        dashboard.getProcessInstancesCard().shouldBe(VISIBLE);
        dashboard.getUserTasksCard().shouldBe(VISIBLE);
        dashboard.getRecentActivityCard().shouldBe(VISIBLE);
        dashboard.getRunningInstancesCard().shouldBe(VISIBLE);
        dashboard.getIncidentsCard().shouldBe(VISIBLE);
    }
}
