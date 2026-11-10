/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine;

import io.jmix.core.DataManager;
import io.flowset.control.entity.EngineConnectionCheckResult;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.testcontainers.EngineContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine(selected = false, shared = false)
public class EngineUiServiceTest extends AbstractIntegrationTest {
    @RunningEngine
    EngineContainer<?> engineContainer;

    @Autowired
    EngineUiService engineUiService;

    @Autowired
    DataManager dataManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM control_bpm_engine");
    }

    @Test
    @DisplayName("Successful connection to BPM engine")
    void givenExistingEngine_whenTestConnection_thenResultIsSuccessful() {
        //given
        BpmEngine bpmEngine = createBpmEngineFromContainer();
        bpmEngine = dataManager.save(bpmEngine);

        //when
        EngineConnectionCheckResult result = engineUiService.checkConnection(bpmEngine);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getVersion()).isEqualTo(engineContainer.getVersion());
    }

    @Test
    @DisplayName("Get version for existing BPM engine")
    void givenExistingEngine_whenGetVersion_thenVersionReturned() {
        //given
        BpmEngine bpmEngine = createBpmEngineFromContainer();
        bpmEngine = dataManager.save(bpmEngine);

        //when
        String result = engineUiService.getVersion(bpmEngine);

        //then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(engineContainer.getVersion());
    }

    @Test
    @DisplayName("Connection check is not supported for new instance of BPM engine")
    void givenNewEngine_whenTestConnection_thenResultIsNotSuccessful() {
        //given
        BpmEngine bpmEngine = createBpmEngineFromContainer();

        //when
        EngineConnectionCheckResult result = engineUiService.checkConnection(bpmEngine);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isFalse();
    }


    @Test
    @DisplayName("IllegalArgumentException is thrown when test engine connection with invalid URL")
    void givenExistingEngineWithInvalidUrl_whenTestConnection_thenIllegalArgumentExceptionThrown() {
        //given
        BpmEngine bpmEngine = createBpmEngineFromContainer();
        bpmEngine.setBaseUrl("not valid url");
        BpmEngine savedEngine = dataManager.save(bpmEngine);

        //when and then
        assertThatThrownBy(() -> engineUiService.checkConnection(savedEngine)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("IllegalArgumentException is thrown when get version with invalid URL")
    void givenExistingEngineWithInvalidUrl_whenGetVersion_thenIllegalArgumentExceptionThrown() {
        //given
        BpmEngine bpmEngine = createBpmEngineFromContainer();
        bpmEngine.setBaseUrl("not valid url");
        BpmEngine savedEngine = dataManager.save(bpmEngine);

        //when and then
        assertThatThrownBy(() -> engineUiService.getVersion(savedEngine)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("No successful connection to BPM engine if service is not available")
    void givenExistingEngineAndStoppedCamunda_whenTestConnection_thenResultIsSuccessful() {
        //given
        BpmEngine bpmEngine = createBpmEngineFromContainer();

        engineContainer.stop();

        //when
        EngineConnectionCheckResult result = engineUiService.checkConnection(bpmEngine);
        engineContainer.start();

        //then
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getVersion()).isNull();
    }

    @Test
    @DisplayName("Connection check fails for engine of unknown type")
    void givenExistingEngineOfUnknownType_whenTestConnection_thenResultIsNotSuccessful() {
        //given
        BpmEngine bpmEngine = dataManager.save(createBpmEngineFromContainer());
        jdbcTemplate.update("UPDATE control_bpm_engine SET TYPE_ = 'UNKNOWN' WHERE ID = ?", bpmEngine.getId());

        //when
        EngineConnectionCheckResult result = engineUiService.checkConnection(bpmEngine);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getVersion()).isNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException is thrown when get version of engine without type")
    void givenNewEngineWithoutType_whenGetVersion_thenEngineConnectionFailedExceptionThrown() {
        //given
        BpmEngine bpmEngine = createBpmEngineFromContainer();
        bpmEngine.setType(null);

        //when and then
        assertThatThrownBy(() -> engineUiService.getVersion(bpmEngine)).isInstanceOf(EngineConnectionFailedException.class);
    }

    private BpmEngine createBpmEngineFromContainer() {
        BpmEngine bpmEngine = dataManager.create(BpmEngine.class);
        bpmEngine.setName("Test Engine " + UUID.randomUUID());
        bpmEngine.setType(engineContainer.getEngineType());
        bpmEngine.setBaseUrl(engineContainer.getRestBaseUrl());
        if (engineContainer.isBasicAuthEnabled()) {
            bpmEngine.setAuthEnabled(true);
            bpmEngine.setAuthType(AuthType.BASIC);
            bpmEngine.setBasicAuthUsername(engineContainer.getBasicAuthUsername());
            bpmEngine.setBasicAuthPassword(engineContainer.getBasicAuthPassword());
        }

        return bpmEngine;
    }
}
