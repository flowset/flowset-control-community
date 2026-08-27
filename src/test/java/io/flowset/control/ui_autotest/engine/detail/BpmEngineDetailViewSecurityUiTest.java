/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.ui_autotest.engine.detail;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.AbstractUiTest;
import io.flowset.control.test_support.ui.view.engine.BpmEngineDetailView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.Masquerade.$j;

@DisplayName("Security on BPM engine detail view")
@Tag("security")
public class BpmEngineDetailViewSecurityUiTest extends AbstractUiTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME like 'test-user%'");
        jdbcTemplate.update(
                "delete from SEC_RESOURCE_POLICY where ROLE_ID in " +
                        "(select ID from SEC_RESOURCE_ROLE where CODE like 'test-%')"
        );
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE like 'test-%'");
        jdbcTemplate.update("delete from USER_ where USERNAME like 'test-user%'");
        jdbcTemplate.update("delete from CONTROL_BPM_ENGINE where NAME like 'test-engine-%'");
    }

    @Test
    @DisplayName("Direct URL navigation is denied without BPM engine detail view policy")
    void givenUserWithoutBpmEngineDetailViewPolicy_whenOpenDetailViewByUrl_thenNavigationDenied() {
        // given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine(
                "test-engine-" + System.currentTimeMillis(), false
        );
        controlTestDataCreator.createUser(
                "test-user-bpm-engine-no-detail-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-bpm-engine-no-detail-view-policy", "password");

        // when
        open("/bpm/engines/" + engine.getId());

        // then
        $j(BpmEngineDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/engines/" + engine.getId() + "'"));
    }

    @Test
    @DisplayName("Direct URL navigation is denied without BPM engine create view policy")
    void givenUserWithoutBpmEngineCreateViewPolicy_whenOpenCreateViewByUrl_thenNavigationDenied() {
        // given
        controlTestDataCreator.createUser(
                "test-user-bpm-engine-no-create-view-policy",
                "password",
                UiMinimalRole.class
        );
        loginAs("test-user-bpm-engine-no-create-view-policy", "password");

        // when
        open("/bpm/engines/new");

        // then
        $j(BpmEngineDetailView.class).shouldNotBe(EXIST);
        $("body").shouldHave(text("Could not navigate to 'bpm/engines/new'"));
    }
}
