/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine.detail;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;

import static io.flowset.control.test_support.ui.UiTestSupport.DATA_SAVING_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.EXIST;

@DisplayName("Actions on BPM engine detail view")
public class BpmEngineDetailViewActionsUiTest extends AbstractUiTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("delete from CONTROL_BPM_ENGINE where NAME like 'test-engine-%'");
    }

    @Test
    @DisplayName("Save action closes BPM engine detail view")
    void givenExistingBpmEngine_whenOpenEngineDetail_thenSaveActionEnabledRuleApplied() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, true);

        MainView mainView = loginAsAdmin();
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // when
        BpmEngineDetailView detailView = listView.openBpmEngineDetailView(engineName);
        detailView.getSaveAndCloseButton().click();

        // then
        detailView.shouldNotBe(EXIST, Duration.ofSeconds(DATA_SAVING_WAIT_DURATION_SEC));
    }

    @Test
    @DisplayName("Close action closes BPM engine detail view")
    void givenExistingBpmEngine_whenOpenEngineDetail_thenCloseActionEnabledRuleApplied() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        controlTestDataCreator.createRandomBpmEngine(engineName, true);

        MainView mainView = loginAsAdmin();
        BpmEngineListView listView = mainView.openBpmEngineListView();

        // when
        BpmEngineDetailView detailView = listView.openBpmEngineDetailView(engineName);
        detailView.getCloseButton().click();

        // then
        detailView.shouldNotBe(EXIST);
    }
}
