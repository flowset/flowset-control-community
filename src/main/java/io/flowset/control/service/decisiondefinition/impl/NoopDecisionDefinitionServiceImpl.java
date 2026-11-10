/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.decisiondefinition.impl;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionLoadContext;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Serves {@link DecisionDefinitionService} calls when no BPM engine is selected.
 */
@EngineServiceDelegate
@Service("control_NoopDecisionDefinitionService")
public class NoopDecisionDefinitionServiceImpl implements DecisionDefinitionService {

    @Override
    public List<DecisionDefinitionData> findLatestVersions() {
        return List.of();
    }

    @Override
    public List<DecisionDefinitionData> findAll(DecisionDefinitionLoadContext context) {
        return List.of();
    }

    @Override
    public long getCount(@Nullable DecisionDefinitionFilter filter) {
        return 0;
    }

    @Override
    public List<DecisionDefinitionData> findAllByKey(String decisionDefinitionKey) {
        return List.of();
    }

    @Nullable
    @Override
    public DecisionDefinitionData getById(String decisionDefinitionId) {
        return null;
    }

    @Nullable
    @Override
    public String getDmnXml(String decisionDefinitionId) {
        return null;
    }

    @Override
    public List<DecisionDefinitionData> findAllByIds(Collection<String> ids) {
        return List.of();
    }
}
