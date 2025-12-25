/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import io.jmix.core.*;
import io.jmix.core.impl.session.ThreadLocalSessionData;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.UiEventPublisher;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.flowset.control.entity.engine.BpmEngine.SELECTED_ENGINE_ATTRIBUTE;

@Service("control_EngineService")
@Slf4j
public class EngineServiceImpl implements EngineService {
    protected final Metadata metadata;
    protected final ObjectProvider<SessionData> sessionDataProvider;
    protected final DataManager dataManager;
    protected final UiEventPublisher uiEventPublisher;
    protected final CurrentAuthentication currentAuthentication;

    public EngineServiceImpl(Metadata metadata,
                             ObjectProvider<SessionData> sessionDataProvider,
                             UiEventPublisher uiEventPublisher,
                             CurrentAuthentication currentAuthentication, DataManager dataManager) {
        this.metadata = metadata;
        this.sessionDataProvider = sessionDataProvider;
        this.uiEventPublisher = uiEventPublisher;
        this.currentAuthentication = currentAuthentication;
        this.dataManager = dataManager;
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

        return dataManager.save(saveContext);
    }

    @Override
    public void markAsDefault(BpmEngine engine) {
        engine.setIsDefault(true);
        saveEngine(engine);
    }

    protected String getSelectedEngineAttribute() {
        if (ThreadLocalSessionData.isSet()) {
            return (String) ThreadLocalSessionData.getAttribute(SELECTED_ENGINE_ATTRIBUTE);
        } else {
            return (String) sessionDataProvider.getObject().getAttribute(SELECTED_ENGINE_ATTRIBUTE);
        }
    }
}