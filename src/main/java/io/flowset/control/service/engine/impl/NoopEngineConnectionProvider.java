/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import io.flowset.control.entity.EngineConnectionCheckResult;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.service.engine.EngineConnectionProvider;
import io.flowset.control.service.engine.ZoneOffsetSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Serves {@link EngineConnectionProvider} calls for an engine without a known type: the engine cannot be connected.
 */
@Component("control_NoopEngineConnectionProvider")
public class NoopEngineConnectionProvider implements EngineConnectionProvider {

    @Override
    public void check(BpmEngine engine, EngineConnectionCheckResult result) {
        result.setSuccess(false);
    }

    @Override
    public String getVersion(BpmEngine engine) {
        String message = "Unknown type of engine '" + engine.getBaseUrl() + "'";
        throw new EngineConnectionFailedException(message, -1, message);
    }

    @Override
    public List<ZoneOffsetSource> getZoneOffsetSources(BpmEngine engine) {
        return List.of();
    }
}
