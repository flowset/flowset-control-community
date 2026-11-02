/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable.impl;

import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.service.variable.VariableLoadContext;
import io.flowset.control.service.variable.VariableService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Serves {@link VariableService} calls when no BPM engine is selected: loads return nothing, operations throw
 * {@link EngineNotSelectedException}.
 */
@EngineServiceDelegate
@Service("control_NoopVariableService")
public class NoopVariableServiceImpl implements VariableService {

    @Override
    public List<VariableInstanceData> findRuntimeVariables(VariableLoadContext loadContext) {
        return List.of();
    }

    @Nullable
    @Override
    public VariableInstanceData findRuntimeVariableById(String variableInstanceId) {
        return null;
    }

    @Nullable
    @Override
    public Resource getVariableInstanceBinary(String variableInstanceId) {
        return null;
    }

    @Override
    public List<HistoricVariableInstanceData> findHistoricVariables(VariableLoadContext loadContext) {
        return List.of();
    }

    @Override
    public long getRuntimeVariablesCount(@Nullable VariableFilter filter) {
        return 0;
    }

    @Override
    public long getHistoricVariablesCount(@Nullable VariableFilter filter) {
        return 0;
    }

    @Override
    public void updateVariableLocal(VariableInstanceData variableInstanceData) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Nullable
    @Override
    public HistoricVariableInstanceData findHistoricVariableById(String variableInstanceId) {
        return null;
    }

    @Override
    public void removeVariableLocal(VariableInstanceData variableInstanceData) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void removeVariablesLocal(Set<VariableInstanceData> variableItems) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void updateVariableBinary(VariableInstanceData variableInstanceData, File data) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }
}
