/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.auth;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineAuthState;
import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * Manages and provides data about engine authentication state.
 */
public interface EngineAuthStateService {

    /**
     * Checks if the engine is locked for getting an access token.
     *
     * @param engineId BPM engine identifier.
     * @return true if the engine is locked, false otherwise.
     */
    boolean isLocked(UUID engineId);

    /**
     * Updates a count of fail attempts in case of getting an access token for the specified engine.
     *
     * @param engineId BPM engine identifier.
     */
    void registerAccessTokenFailure(BpmEngine engineId);

    /**
     * Resets the fail attempts count of getting an access token for the specified engine.
     *
     * @param engineId BPM engine identifier.
     */
    void unlock(UUID engineId);

    /**
     * Finds engine authentication state by BPM engine identifier.
     *
     * @param engineId BPM engine identifier.
     * @return engine authentication state or null if not found.
     */
    @Nullable
    EngineAuthState findByEngineId(UUID engineId);
}
