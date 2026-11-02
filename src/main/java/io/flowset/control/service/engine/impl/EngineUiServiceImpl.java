/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import io.jmix.core.*;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.UiEventPublisher;
import io.flowset.control.entity.EngineConnectionCheckResult;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.event.UserEngineSelectEvent;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.EngineUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

@Service("control_EngineUiService")
@Slf4j
public class EngineUiServiceImpl implements EngineUiService {
    protected final Metadata metadata;
    protected final ObjectProvider<SessionData> sessionDataProvider;
    protected final DataManager dataManager;
    protected final UiEventPublisher uiEventPublisher;
    protected final CurrentAuthentication currentAuthentication;
    protected final EngineService engineService;
    protected final EngineTimeResolver engineTimeResolver;
    protected final EngineZoneOffsetResolver engineZoneOffsetResolver;
    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public EngineUiServiceImpl(Metadata metadata,
                               ObjectProvider<SessionData> sessionDataProvider,
                               UiEventPublisher uiEventPublisher,
                               CurrentAuthentication currentAuthentication, DataManager dataManager,
                               EngineService engineService,
                               EngineTimeResolver engineTimeResolver,
                               EngineZoneOffsetResolver engineZoneOffsetResolver,
                               EngineOperationServiceProvider engineOperationServiceProvider) {
        this.metadata = metadata;
        this.sessionDataProvider = sessionDataProvider;
        this.uiEventPublisher = uiEventPublisher;
        this.currentAuthentication = currentAuthentication;
        this.dataManager = dataManager;
        this.engineService = engineService;
        this.engineTimeResolver = engineTimeResolver;
        this.engineZoneOffsetResolver = engineZoneOffsetResolver;
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }


    @Override
    public EngineConnectionCheckResult checkConnection(BpmEngine bpmEngine) {
        EngineConnectionCheckResult result = metadata.create(EngineConnectionCheckResult.class);

        BpmEngine persistedEngine = engineService.findEngineByUuid(bpmEngine.getId());
        if (persistedEngine == null) {
            log.error("There is no engine with id  {}", bpmEngine.getId());
            result.setSuccess(false);
            return result;
        }

        engineOperationServiceProvider.getConnectionProvider(persistedEngine.getType()).check(persistedEngine, result);
        return result;
    }

    @Override
    public String getVersion(BpmEngine engine) {
        return engineOperationServiceProvider.getConnectionProvider(engine.getType()).getVersion(engine);
    }

    @Override
    public void selectEngine(BpmEngine engine) {
        String username = currentAuthentication.getUser().getUsername();
        log.debug("User '{}' selects engine '{} (name: '{}', url: '{}')'", username, engine.getId(), engine.getName(), engine.getBaseUrl());
        engineService.setSelectedEngine(engine);
        uiEventPublisher.publishEvent(new UserEngineSelectEvent(this, engine, username));
    }

    @TransactionalEventListener
    public void onBpmEngineChangedAfterCommit(final EntityChangedEvent<BpmEngine> event) {
        Id<BpmEngine> entityId = event.getEntityId();
        if (event.getType() == EntityChangedEvent.Type.DELETED
                || event.getType() == EntityChangedEvent.Type.UPDATED) {
            UUID engineId = (UUID) entityId.getValue();

            engineTimeResolver.unregisterEngine(engineId);
            engineZoneOffsetResolver.invalidate(engineId);
        }
    }

}
