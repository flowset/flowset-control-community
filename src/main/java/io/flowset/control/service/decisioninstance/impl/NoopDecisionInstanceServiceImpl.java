/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.decisioninstance.impl;

import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.service.decisioninstance.DecisionInstanceLoadContext;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link DecisionInstanceService} calls when no BPM engine is selected.
 */
@EngineServiceDelegate
@Service("control_NoopDecisionInstanceService")
public class NoopDecisionInstanceServiceImpl implements DecisionInstanceService {

    @Override
    public List<HistoricDecisionInstanceShortData> findAllHistoryDecisionInstances(
            DecisionInstanceLoadContext loadContext) {
        return List.of();
    }

    @Nullable
    @Override
    public HistoricDecisionInstanceShortData getById(String decisionInstanceId) {
        return null;
    }

    @Override
    public long getHistoryDecisionInstancesCount(@Nullable DecisionInstanceFilter filter) {
        return 0;
    }

    @Override
    public long getCountByDecisionDefinitionId(String decisionDefinitionId) {
        return 0;
    }

    @Override
    public long getCountByDecisionDefinitionKey(String decisionDefinitionKey) {
        return 0;
    }
}
