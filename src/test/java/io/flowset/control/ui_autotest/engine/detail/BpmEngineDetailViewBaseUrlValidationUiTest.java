/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine.detail;

import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.view.MainView;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;
import static io.flowset.control.test_support.ui.UiTestSupport.DATA_SAVING_WAIT_DURATION_SEC;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Base URL validation on BPM engine detail view")
public class BpmEngineDetailViewBaseUrlValidationUiTest extends AbstractUiTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("delete from CONTROL_BPM_ENGINE where NAME like 'test-engine-%'");
    }

    @Test
    @DisplayName("Engine is not saved if Base URL is empty")
    void givenNewEngine_whenSaveWithEmptyBaseUrl_thenValidationErrorShown() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        MainView mainView = loginAsAdmin();
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue(engineName);
        detailView.getBaseUrlField().setValue("");

        // when
        detailView.getSaveAndCloseButton().click();

        // then
        detailView.getBaseUrlField().getDelegate()
                .shouldHave(attribute("invalid"))
                .$("[slot='error-message']")
                .shouldHave(text("Base URL required"));

        detailView.shouldBe(EXIST);
    }

    @Test
    @DisplayName("Engine is not saved if Base URL is not a URL")
    void givenNewEngine_whenSaveWithNotParsableBaseUrl_thenValidationErrorShown() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        MainView mainView = loginAsAdmin();
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue(engineName);
        detailView.getBaseUrlField().setValue("qqq");

        // when
        detailView.getSaveAndCloseButton().click();

        // then
        detailView.getBaseUrlField().getDelegate()
                .shouldHave(attribute("invalid"))
                .$("[slot='error-message']")
                .shouldHave(text("Invalid URL 'qqq'"));

        detailView.shouldBe(EXIST);
    }

    @Test
    @DisplayName("Engine is not saved if Base URL has a scheme other than HTTP or HTTPS")
    void givenNewEngine_whenSaveWithUnsupportedBaseUrlScheme_thenValidationErrorShown() {
        // given
        String engineName = "test-engine-" + System.currentTimeMillis();
        String baseUrl = "ftp://localhost:8080/engine-rest";

        MainView mainView = loginAsAdmin();
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue(engineName);
        detailView.getBaseUrlField().setValue(baseUrl);

        // when
        detailView.getSaveAndCloseButton().click();

        // then
        detailView.getBaseUrlField().getDelegate()
                .shouldHave(attribute("invalid"))
                .$("[slot='error-message']")
                .shouldHave(text("Invalid URL '%s'".formatted(baseUrl)));

        detailView.shouldBe(EXIST);
    }

    @Test
    @DisplayName("Engine is saved if Base URL is an HTTP URL")
    void givenNewEngine_whenSaveWithHttpBaseUrl_thenEngineSaved() {
        // given
        long suffix = System.currentTimeMillis();
        String engineName = "test-engine-" + suffix;
        String baseUrl = "http://localhost:8080/engine-rest-" + suffix;

        MainView mainView = loginAsAdmin();
        mainView.openBpmEngineListView().getCreateButton().click();

        BpmEngineDetailView detailView = $j(BpmEngineDetailView.class).exists();
        detailView.getNameField().setValue(engineName);
        detailView.getBaseUrlField().setValue(baseUrl);

        // when
        detailView.getSaveAndCloseButton().click();

        // then
        detailView.shouldNotBe(EXIST, Duration.ofSeconds(DATA_SAVING_WAIT_DURATION_SEC));
    }
}
