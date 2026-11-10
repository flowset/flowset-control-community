/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine;

import io.flowset.control.entity.EngineConnectionCheckResult;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.exception.EngineConnectionFailedException;

import java.util.List;

/**
 * Connects to the BPM engines of one engine type: checks the connection, loads the engine version and tells
 * where the engine serializes dates with the offset of its server. The implementation for an engine type is
 * picked by {@link EngineOperationServiceProvider#getConnectionProvider(EngineType)}.
 */
public interface EngineConnectionProvider {

    /**
     * Checks the connection to the engine and fills the result: the success flag and, if connected, the version.
     *
     * @param engine the engine to check
     * @param result the result to fill
     */
    void check(BpmEngine engine, EngineConnectionCheckResult result);

    /**
     * Returns the version of the engine.
     *
     * @param engine the engine to get the version of
     * @return the version reported by the engine
     * @throws EngineConnectionFailedException if the engine is not available
     */
    String getVersion(BpmEngine engine);

    /**
     * Returns the requests to obtain a date serialized by the engine with the offset of its server, in order of
     * priority.
     *
     * @param engine the engine to build the requests for
     * @return the requests in order of priority, or an empty list if the offset cannot be obtained from the engine
     */
    List<ZoneOffsetSource> getZoneOffsetSources(BpmEngine engine);
}
