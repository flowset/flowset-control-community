/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable.impl;

import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.variable.VariableLoadContext;
import io.flowset.control.service.variable.VariableService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Delegates to the {@link VariableService} of the selected engine type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_VariableService")
public class VariableServiceImpl implements VariableService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public VariableServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<VariableInstanceData> findRuntimeVariables(VariableLoadContext loadContext) {
        return engineOperationServiceProvider.getVariableService().findRuntimeVariables(loadContext);
    }

    @Override
    public VariableInstanceData findRuntimeVariableById(String variableInstanceId) {
        return engineOperationServiceProvider.getVariableService().findRuntimeVariableById(variableInstanceId);
    }

    @Override
    public Resource getVariableInstanceBinary(String variableInstanceId) {
        return engineOperationServiceProvider.getVariableService().getVariableInstanceBinary(variableInstanceId);
    }

    @Override
    public List<HistoricVariableInstanceData> findHistoricVariables(VariableLoadContext loadContext) {
        return engineOperationServiceProvider.getVariableService().findHistoricVariables(loadContext);
    }

    @Override
    public long getRuntimeVariablesCount(@Nullable VariableFilter filter) {
        return engineOperationServiceProvider.getVariableService().getRuntimeVariablesCount(filter);
    }

    @Override
    public long getHistoricVariablesCount(@Nullable VariableFilter filter) {
        return engineOperationServiceProvider.getVariableService().getHistoricVariablesCount(filter);
    }

    @Override
    public void updateVariableLocal(VariableInstanceData variableInstanceData) {
        engineOperationServiceProvider.getVariableService().updateVariableLocal(variableInstanceData);
    }

    @Override
    public HistoricVariableInstanceData findHistoricVariableById(String variableInstanceId) {
        return engineOperationServiceProvider.getVariableService().findHistoricVariableById(variableInstanceId);
    }

    @Override
    public void removeVariableLocal(VariableInstanceData variableInstanceData) {
        engineOperationServiceProvider.getVariableService().removeVariableLocal(variableInstanceData);
    }

    @Override
    public void removeVariablesLocal(Set<VariableInstanceData> variableItems) {
        engineOperationServiceProvider.getVariableService().removeVariablesLocal(variableItems);
    }

    @Override
    public void updateVariableBinary(VariableInstanceData variableInstanceData, File data) {
        engineOperationServiceProvider.getVariableService().updateVariableBinary(variableInstanceData, data);
    }
}
