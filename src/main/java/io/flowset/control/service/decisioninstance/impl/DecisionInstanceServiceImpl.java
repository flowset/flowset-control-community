/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.decisioninstance.impl;

import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.service.decisioninstance.DecisionInstanceLoadContext;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link DecisionInstanceService} of the selected engine type, see
 * {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_DecisionInstanceService")
public class DecisionInstanceServiceImpl implements DecisionInstanceService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public DecisionInstanceServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<HistoricDecisionInstanceShortData> findAllHistoryDecisionInstances(
            DecisionInstanceLoadContext loadContext) {
        return engineOperationServiceProvider.getDecisionInstanceService()
                .findAllHistoryDecisionInstances(loadContext);
    }

    @Nullable
    @Override
    public HistoricDecisionInstanceShortData getById(String decisionInstanceId) {
        return engineOperationServiceProvider.getDecisionInstanceService().getById(decisionInstanceId);
    }

    @Override
    public long getHistoryDecisionInstancesCount(@Nullable DecisionInstanceFilter filter) {
        return engineOperationServiceProvider.getDecisionInstanceService().getHistoryDecisionInstancesCount(filter);
    }

    @Override
    public long getCountByDecisionDefinitionId(String decisionDefinitionId) {
        return engineOperationServiceProvider.getDecisionInstanceService()
                .getCountByDecisionDefinitionId(decisionDefinitionId);
    }

    @Override
    public long getCountByDecisionDefinitionKey(String decisionDefinitionKey) {
        return engineOperationServiceProvider.getDecisionInstanceService()
                .getCountByDecisionDefinitionKey(decisionDefinitionKey);
    }
}
