/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance.impl;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.service.processinstance.ProcessInstanceBulkTerminateContext;
import io.flowset.control.service.processinstance.ProcessInstanceLoadContext;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Serves {@link ProcessInstanceService} calls when no BPM engine is selected: loads return nothing, operations throw
 * {@link EngineNotSelectedException}.
 */
@EngineServiceDelegate
@Service("control_NoopProcessInstanceService")
public class NoopProcessInstanceServiceImpl implements ProcessInstanceService {

    @Override
    public List<ProcessInstanceData> findAllHistoricInstances(ProcessInstanceLoadContext loadContext) {
        return List.of();
    }

    @Override
    public List<RuntimeProcessInstanceData> findAllRuntimeInstances(ProcessInstanceLoadContext loadContext) {
        return List.of();
    }

    @Override
    public long getHistoricInstancesCount(ProcessInstanceFilter filter) {
        return 0;
    }

    @Override
    public long getRuntimeInstancesCount(ProcessInstanceFilter filter) {
        return 0;
    }

    @Nullable
    @Override
    public ProcessInstanceData getProcessInstanceById(String processInstanceId) {
        return null;
    }

    @Override
    public ProcessInstanceData startProcessByDefinitionId(String processDefinitionId,
                                                          Collection<VariableInstanceData> variableInstances,
                                                          @Nullable String businessKey) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void suspendById(String processInstanceId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void activateById(String processInstanceId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void terminateById(String processInstanceId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public BatchData terminateByIdsAsync(ProcessInstanceBulkTerminateContext context) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public BatchData activateByIdsAsync(List<String> processInstancesIds) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public BatchData suspendByIdsAsync(List<String> processInstancesIds) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public long getCountByProcessDefinitionId(String processDefinitionId) {
        return 0;
    }

    @Override
    public long getCountByDeploymentId(String deploymentId) {
        return 0;
    }

    @Override
    public long getCountByProcessDefinitionKey(String processDefinitionKey) {
        return 0;
    }
}
