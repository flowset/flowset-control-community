/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class EngineServiceTest extends AbstractIntegrationTest {

    @Autowired
    EngineService engineService;
    @Autowired
    ControlTestDataCreator controlTestDataCreator;
    @Autowired
    DataManager dataManager;

    @AfterEach
    void tearDown() {
        resetSelectedEngine();
    }

    @Test
    @DisplayName("Engine found by the same base URL")
    void givenEngine_whenFindEngineBySameBaseUrl_thenEngineFound() {
        //given
        BpmEngine engine = controlTestDataCreator.createBpmEngine("Engine", "http://localhost:8080/engine-rest", true);

        //when
        BpmEngine foundEngine = engineService.findEngineByBaseUrl("http://localhost:8080/engine-rest");

        //then
        assertThat(foundEngine).isNotNull();
        assertThat(foundEngine.getId()).isEqualTo(engine.getId());
    }

    @Test
    @DisplayName("Engine found by base URL with a trailing slash")
    void givenEngineWithoutTrailingSlash_whenFindEngineByUrlWithTrailingSlash_thenEngineFound() {
        //given
        BpmEngine engine = controlTestDataCreator.createBpmEngine("Engine", "http://localhost:8080/engine-rest", true);

        //when
        BpmEngine foundEngine = engineService.findEngineByBaseUrl("http://localhost:8080/engine-rest/");

        //then
        assertThat(foundEngine).isNotNull();
        assertThat(foundEngine.getId()).isEqualTo(engine.getId());
    }

    @Test
    @DisplayName("Engine found by base URL without a trailing slash")
    void givenEngineWithTrailingSlash_whenFindEngineByUrlWithoutTrailingSlash_thenEngineFound() {
        //given
        BpmEngine engine = controlTestDataCreator.createBpmEngine("Engine", "http://localhost:8080/engine-rest/", true);

        //when
        BpmEngine foundEngine = engineService.findEngineByBaseUrl("  http://localhost:8080/engine-rest ");

        //then
        assertThat(foundEngine).isNotNull();
        assertThat(foundEngine.getId()).isEqualTo(engine.getId());
    }

    @Test
    @DisplayName("Engine with the exact base URL preferred")
    void givenEnginesWithAndWithoutTrailingSlash_whenFindEngineByBaseUrl_thenExactMatchFound() {
        //given
        controlTestDataCreator.createBpmEngine("Engine 1", "http://localhost:8080/engine-rest", true);
        BpmEngine engineWithSlash = controlTestDataCreator.createBpmEngine("Engine 2",
                "http://localhost:8080/engine-rest/", false);

        //when
        BpmEngine foundEngine = engineService.findEngineByBaseUrl("http://localhost:8080/engine-rest/");

        //then
        assertThat(foundEngine).isNotNull();
        assertThat(foundEngine.getId()).isEqualTo(engineWithSlash.getId());
    }

    @Test
    @DisplayName("No engine found by unknown base URL")
    void givenEngine_whenFindEngineByUnknownUrl_thenNull() {
        //given
        controlTestDataCreator.createBpmEngine("Engine", "http://localhost:8080/engine-rest", true);

        //when
        BpmEngine foundEngine = engineService.findEngineByBaseUrl("http://localhost:9090/engine-rest");

        //then
        assertThat(foundEngine).isNull();
    }

    @Test
    @DisplayName("No engine found by blank base URL")
    void givenEngine_whenFindEngineByBlankUrl_thenNull() {
        //given
        controlTestDataCreator.createBpmEngine("Engine", "http://localhost:8080/engine-rest", true);

        //when
        BpmEngine foundEngine = engineService.findEngineByBaseUrl(" ");

        //then
        assertThat(foundEngine).isNull();
    }

    @Test
    @DisplayName("Engine type of selected engine returned")
    void givenSelectedEngine_whenGetSelectedEngineType_thenTypeOfSelectedEngineReturned() {
        //given
        controlTestDataCreator.createRandomBpmEngine("Engine 1", true);
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine 2", false);
        engine.setType(EngineType.OPERATON);
        engine = dataManager.save(engine);
        engineService.setSelectedEngine(engine);

        //when
        EngineType selectedEngineType = engineService.getSelectedEngineType();

        //then
        assertThat(selectedEngineType).isEqualTo(EngineType.OPERATON);
    }

    @Test
    @DisplayName("Engine type of default engine returned when no engine selected")
    void givenNoSelectedEngine_whenGetSelectedEngineType_thenTypeOfDefaultEngineReturned() {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(EngineType.CIB_SEVEN);
        dataManager.save(engine);

        //when
        EngineType selectedEngineType = engineService.getSelectedEngineType();

        //then
        assertThat(selectedEngineType).isEqualTo(EngineType.CIB_SEVEN);
    }

    @Test
    @DisplayName("Engine type of new default engine returned")
    void givenOtherEngineMarkedAsDefault_whenGetSelectedEngineType_thenTypeOfNewDefaultEngineReturned() {
        //given
        controlTestDataCreator.createRandomBpmEngine("Engine 1", true);
        engineService.getSelectedEngineType();

        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine 2", false);
        engine.setType(EngineType.FLUXNOVA);
        engine = dataManager.save(engine);
        engineService.markAsDefault(engine);

        //when
        EngineType selectedEngineType = engineService.getSelectedEngineType();

        //then
        assertThat(selectedEngineType).isEqualTo(EngineType.FLUXNOVA);
    }

    @Test
    @DisplayName("Changed engine type of selected engine returned")
    void givenSelectedEngineTypeChanged_whenGetSelectedEngineType_thenNewTypeReturned() {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engineService.setSelectedEngine(engine);
        engineService.getSelectedEngineType();

        engine.setType(EngineType.FLUXNOVA);
        dataManager.save(engine);

        //when
        EngineType selectedEngineType = engineService.getSelectedEngineType();

        //then
        assertThat(selectedEngineType).isEqualTo(EngineType.FLUXNOVA);
    }

    @Test
    @DisplayName("Engine type of default engine returned when selected engine deleted")
    void givenSelectedEngineDeleted_whenGetSelectedEngineType_thenTypeOfDefaultEngineReturned() {
        //given
        BpmEngine defaultEngine = controlTestDataCreator.createRandomBpmEngine("Engine 1", true);
        defaultEngine.setType(EngineType.OPERATON);
        dataManager.save(defaultEngine);

        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine 2", false);
        engineService.setSelectedEngine(engine);
        engineService.getSelectedEngineType();

        dataManager.remove(engine);

        //when
        EngineType selectedEngineType = engineService.getSelectedEngineType();

        //then
        assertThat(selectedEngineType).isEqualTo(EngineType.OPERATON);
    }

    @Test
    @DisplayName("No selected engine type when no engine exists")
    void givenNoEngine_whenGetSelectedEngineType_thenNull() {
        //when
        EngineType selectedEngineType = engineService.getSelectedEngineType();

        //then
        assertThat(selectedEngineType).isNull();
    }
}
