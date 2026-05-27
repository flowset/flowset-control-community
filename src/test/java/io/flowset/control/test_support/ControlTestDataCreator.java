/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.entity.engine.EnvironmentType;
import io.jmix.core.UnconstrainedDataManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * For creating test data in the Control database.
 */
@Slf4j
@Component("control_ControlTestDataCreator")
public class ControlTestDataCreator {

    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager;

    /**
     * Creates a BPM engine entity with the specified parameters and saves it to the database.
     *
     * @param name      BPM engine name
     * @param baseUrl   a base URL for the BPM engine
     * @param isDefault whether this engine is the default one
     * @return created and saved to the Control database BPM engine entity
     */
    public BpmEngine createBpmEngine(String name, String baseUrl, boolean isDefault) {
        BpmEngine engine = unconstrainedDataManager.create(BpmEngine.class);
        engine.setName(name);
        engine.setType(EngineType.CAMUNDA_7);
        engine.setEnvironmentType(EnvironmentType.LOCAL);
        engine.setBaseUrl(baseUrl);
        engine.setIsDefault(isDefault);

        BpmEngine saved = unconstrainedDataManager.save(engine);

        log.info("Created and saved BPM engine: {}, id: '{}', url: '{}'", saved.getName(), saved.getId(), saved.getBaseUrl());
        return saved;
    }

    /**
     * Creates and saves BPM engine entity with the specified name and random URL.
     *
     * @param name      BPM engine name
     * @param isDefault whether this engine is the default one
     * @return created and saved to the Control database BPM engine
     */
    public BpmEngine createRandomBpmEngine(String name, boolean isDefault) {
        String engineUrl = "http://%s.invalid/engine-rest".formatted(UUID.randomUUID());
        return createBpmEngine(name, engineUrl, isDefault);
    }
}
