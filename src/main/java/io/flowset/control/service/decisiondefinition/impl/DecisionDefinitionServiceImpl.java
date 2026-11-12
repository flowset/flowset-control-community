/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.decisiondefinition.impl;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionLoadContext;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Delegates to the {@link DecisionDefinitionService} of the selected engine type, see
 * {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_DecisionDefinitionService")
public class DecisionDefinitionServiceImpl implements DecisionDefinitionService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public DecisionDefinitionServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<DecisionDefinitionData> findLatestVersions() {
        return engineOperationServiceProvider.getDecisionDefinitionService().findLatestVersions();
    }

    @Override
    public List<DecisionDefinitionData> findAll(DecisionDefinitionLoadContext context) {
        return engineOperationServiceProvider.getDecisionDefinitionService().findAll(context);
    }

    @Override
    public long getCount(@Nullable DecisionDefinitionFilter filter) {
        return engineOperationServiceProvider.getDecisionDefinitionService().getCount(filter);
    }

    @Override
    public List<DecisionDefinitionData> findAllByKey(String decisionDefinitionKey) {
        return engineOperationServiceProvider.getDecisionDefinitionService().findAllByKey(decisionDefinitionKey);
    }

    @Nullable
    @Override
    public DecisionDefinitionData getById(String decisionDefinitionId) {
        return engineOperationServiceProvider.getDecisionDefinitionService().getById(decisionDefinitionId);
    }

    @Override
    public String getDmnXml(String decisionDefinitionId) {
        return engineOperationServiceProvider.getDecisionDefinitionService().getDmnXml(decisionDefinitionId);
    }

    @Override
    public List<DecisionDefinitionData> findAllByIds(Collection<String> ids) {
        return engineOperationServiceProvider.getDecisionDefinitionService().findAllByIds(ids);
    }
}
