/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.auth.impl;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineAuthState;
import io.flowset.control.property.EngineOAuth2Properties;
import io.flowset.control.service.engine.auth.EngineAuthStateService;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.querycondition.PropertyCondition;
import jakarta.persistence.PersistenceException;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component("control_EngineAuthStateService")
public class EngineAuthStateServiceImpl implements EngineAuthStateService {
    protected static final Logger log = LoggerFactory.getLogger(EngineAuthStateServiceImpl.class);

    protected final DataManager dataManager;
    protected final EngineOAuth2Properties engineOAuth2Properties;

    public EngineAuthStateServiceImpl(DataManager dataManager,
                                      EngineOAuth2Properties engineOAuth2Properties) {
        this.dataManager = dataManager;
        this.engineOAuth2Properties = engineOAuth2Properties;
    }

    @Override
    public boolean isLocked(UUID engineId) {
        boolean lockEnabled = engineOAuth2Properties.isLockEnabled();
        if (!lockEnabled) {
            return false;
        }
        EngineAuthState state = findByEngineId(engineId);
        return state != null && BooleanUtils.isTrue(state.getIsLocked());
    }

    @Override
    public void registerAccessTokenFailure(BpmEngine engine) {
        boolean lockEnabled = engineOAuth2Properties.isLockEnabled();
        if (!lockEnabled) {
            return;
        }

        EngineAuthState state = findByEngineId(engine.getId());

        if (state == null) {
            createEngineAuthState(engine);
        } else {
            incrementRetries(state);
        }
    }

    @Override
    public void unlock(UUID engineId) {
        boolean lockEnabled = engineOAuth2Properties.isLockEnabled();
        if (!lockEnabled) {
            return;
        }
        EngineAuthState state = findByEngineId(engineId);
        if (state != null) {
            dataManager.remove(state);
        }
    }

    @Nullable
    @Override
    public EngineAuthState findByEngineId(UUID engineId) {
        boolean lockEnabled = engineOAuth2Properties.isLockEnabled();
        if (!lockEnabled) {
            return null;
        }
        return dataManager.load(EngineAuthState.class)
                .condition(PropertyCondition.equal("engine.id", engineId))
                .fetchPlan(FetchPlan.BASE)
                .optional()
                .orElse(null);
    }

    protected void incrementRetries(EngineAuthState state) {
        try {
            if (BooleanUtils.isTrue(state.getIsLocked())) {
                log.debug("OAuth2 token refresh is locked for BPM engine {}", state.getEngine().getId());
                return;
            }
            Integer accessTokenRetries = state.getAccessTokenRetries();
            if (accessTokenRetries == null) {
                accessTokenRetries = 0;
            }
            int newRetries = accessTokenRetries + 1;
            state.setAccessTokenRetries(newRetries);
            if (newRetries >= engineOAuth2Properties.getMaxRetries()) {
                log.debug("OAuth2 token refresh retries exceeded for BPM engine {}", state.getEngine().getId());
                state.setIsLocked(true);
                state.setLockDate(OffsetDateTime.now());
            } else {
                state.setIsLocked(false);
                state.setLockDate(null);
            }
            dataManager.save(state);
        } catch (DataIntegrityViolationException | PersistenceException e) {
            log.warn("Optimistic lock exception while updating auth state for engine {}, error: {}", state.getEngine().getId(), e.getMessage());
        }
    }

    protected void createEngineAuthState(BpmEngine engine) {
        try {
            EngineAuthState state = dataManager.create(EngineAuthState.class);
            state.setEngine(engine);
            state.setAccessTokenRetries(1);
            state.setIsLocked(engineOAuth2Properties.getMaxRetries() <= 1);
            if (state.getIsLocked()) {
                state.setLockDate(OffsetDateTime.now());
            }
            dataManager.save(state);
        } catch (DataIntegrityViolationException | PersistenceException ex) {
            log.warn("Conflict while updating auth state for engine for engine {}", engine, ex);
        }
    }
}
