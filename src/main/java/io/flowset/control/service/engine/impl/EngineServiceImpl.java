/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import io.flowset.control.service.engine.auth.EngineAuthStateService;
import io.jmix.core.*;
import io.jmix.core.impl.session.ThreadLocalSessionData;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.UiEventPublisher;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.service.engine.EngineService;
import io.jmix.core.event.EntityChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.flowset.control.entity.engine.BpmEngine.SELECTED_ENGINE_ATTRIBUTE;

@Service("control_EngineService")
@Slf4j
public class EngineServiceImpl implements EngineService {
    protected final Metadata metadata;
    protected final ObjectProvider<SessionData> sessionDataProvider;
    protected final DataManager dataManager;
    protected final UiEventPublisher uiEventPublisher;
    protected final CurrentAuthentication currentAuthentication;
    protected final EngineAuthStateService engineAuthStateService;
    protected final EntityStates entityStates;

    /**
     * Types of the selected engine by the selected engine session attribute.
     */
    protected final Map<String, EngineType> engineTypeById = new ConcurrentHashMap<>();

    public EngineServiceImpl(Metadata metadata,
                             ObjectProvider<SessionData> sessionDataProvider,
                             UiEventPublisher uiEventPublisher,
                             CurrentAuthentication currentAuthentication,
                             DataManager dataManager,
                             EngineAuthStateService engineAuthStateService,
                             EntityStates entityStates) {
        this.metadata = metadata;
        this.sessionDataProvider = sessionDataProvider;
        this.uiEventPublisher = uiEventPublisher;
        this.currentAuthentication = currentAuthentication;
        this.dataManager = dataManager;
        this.engineAuthStateService = engineAuthStateService;
        this.entityStates = entityStates;
    }

    @Override
    public BpmEngine getSelectedEngine() {
        String sessionAttribute = getSelectedEngineAttribute();
        if (sessionAttribute == null) {
            return findDefaultEngine();
        }

        Optional<BpmEngine> foundBpmEngine = dataManager.load(BpmEngine.class).id(UUID.fromString(sessionAttribute)).optional();
        return foundBpmEngine.orElseGet(this::findDefaultEngine);
    }

    @Override
    public EngineType getSelectedEngineType() {
        String selectedEngineAttribute = getSelectedEngineAttribute();
        if (selectedEngineAttribute == null) {
            BpmEngine engine = findDefaultEngine();
            return engine != null ? engine.getType() : null;
        }

        EngineType engineType = engineTypeById.get(selectedEngineAttribute);
        if (engineType != null) {
            return engineType;
        }

        BpmEngine engine = getSelectedEngine();
        if (engine == null || engine.getType() == null) {
            return null;
        }
        engineTypeById.put(selectedEngineAttribute, engine.getType());
        return engine.getType();
    }

    @Override
    public BpmEngine findDefaultEngine() {
        Optional<BpmEngine> defaultEngine = dataManager.load(BpmEngine.class)
                .condition(PropertyCondition.equal("isDefault", true))
                .optional();

        return defaultEngine.orElseGet(() -> dataManager.load(BpmEngine.class)
                .all()
                .maxResults(1)
                .sort(Sort.by(Sort.Direction.ASC,
                        "createdDate"))
                .optional()
                .orElse(null));
    }

    @Override
    public BpmEngine findEngineByUuid(UUID uuid) {
        Optional<BpmEngine> bpmEngine = dataManager.load(BpmEngine.class)
                .condition(PropertyCondition.equal("id", uuid))
                .optional();

        return bpmEngine.orElse(null);
    }

    @Override
    public BpmEngine findEngineByBaseUrl(String baseUrl) {
        String url = baseUrl.trim();
        if (url.isEmpty()) {
            return null;
        }
        String alternativeUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url + "/";

        return dataManager.load(BpmEngine.class)
                .condition(PropertyCondition.equal("baseUrl", url))
                .optional()
                .or(() -> dataManager.load(BpmEngine.class)
                        .condition(PropertyCondition.equal("baseUrl", alternativeUrl))
                        .optional())
                .orElse(null);
    }

    @Override
    public void setSelectedEngine(BpmEngine engine) {
        sessionDataProvider.getObject().setAttribute(SELECTED_ENGINE_ATTRIBUTE, engine.getId().toString());
    }

    @Override
    public boolean engineExists() {
        long engineCount = dataManager.getCount(new LoadContext<>(metadata.getClass(BpmEngine.class)));
        return engineCount > 0;
    }

    @Override
    public Set<Object> saveEngine(BpmEngine engine) {
        SaveContext saveContext = new SaveContext()
                .saving(engine);

        if (BooleanUtils.isTrue(engine.getIsDefault())) {
            List<BpmEngine> defaultEngines = dataManager.load(BpmEngine.class)
                    .condition(PropertyCondition.equal("isDefault", true))
                    .list();
            defaultEngines.forEach(bpmEngine -> {
                bpmEngine.setIsDefault(false);
                saveContext.saving(bpmEngine);
            });
        }

        Set<Object> savedEntities = dataManager.save(saveContext);

        return savedEntities;
    }

    @Override
    public void markAsDefault(BpmEngine engine) {
        engine.setIsDefault(true);
        saveEngine(engine);
    }

    @TransactionalEventListener
    public void onBpmEngineChangedAfterCommit(final EntityChangedEvent<BpmEngine> event) {
        engineTypeById.clear();
    }

    protected String getSelectedEngineAttribute() {
        if (ThreadLocalSessionData.isSet()) {
            return (String) ThreadLocalSessionData.getAttribute(SELECTED_ENGINE_ATTRIBUTE);
        } else {
            return (String) sessionDataProvider.getObject().getAttribute(SELECTED_ENGINE_ATTRIBUTE);
        }
    }
}
