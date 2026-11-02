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
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.processinstance.ProcessInstanceBulkTerminateContext;
import io.flowset.control.service.processinstance.ProcessInstanceLoadContext;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Delegates to the {@link ProcessInstanceService} of the selected engine type, see
 * {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_ProcessInstanceService")
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public ProcessInstanceServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<ProcessInstanceData> findAllHistoricInstances(ProcessInstanceLoadContext loadContext) {
        return engineOperationServiceProvider.getProcessInstanceService().findAllHistoricInstances(loadContext);
    }

    @Override
    public List<RuntimeProcessInstanceData> findAllRuntimeInstances(ProcessInstanceLoadContext loadContext) {
        return engineOperationServiceProvider.getProcessInstanceService().findAllRuntimeInstances(loadContext);
    }

    @Override
    public long getHistoricInstancesCount(ProcessInstanceFilter filter) {
        return engineOperationServiceProvider.getProcessInstanceService().getHistoricInstancesCount(filter);
    }

    @Override
    public long getRuntimeInstancesCount(ProcessInstanceFilter filter) {
        return engineOperationServiceProvider.getProcessInstanceService().getRuntimeInstancesCount(filter);
    }

    @Override
    public ProcessInstanceData getProcessInstanceById(String processInstanceId) {
        return engineOperationServiceProvider.getProcessInstanceService().getProcessInstanceById(processInstanceId);
    }

    @Override
    public ProcessInstanceData startProcessByDefinitionId(String processDefinitionId,
                                                          Collection<VariableInstanceData> variableInstances,
                                                          @Nullable String businessKey) {
        return engineOperationServiceProvider.getProcessInstanceService()
                .startProcessByDefinitionId(processDefinitionId, variableInstances, businessKey);
    }

    @Override
    public void suspendById(String processInstanceId) {
        engineOperationServiceProvider.getProcessInstanceService().suspendById(processInstanceId);
    }

    @Override
    public void activateById(String processInstanceId) {
        engineOperationServiceProvider.getProcessInstanceService().activateById(processInstanceId);
    }

    @Override
    public void terminateById(String processInstanceId) {
        engineOperationServiceProvider.getProcessInstanceService().terminateById(processInstanceId);
    }

    @Nullable
    @Override
    public BatchData terminateByIdsAsync(ProcessInstanceBulkTerminateContext context) {
        return engineOperationServiceProvider.getProcessInstanceService().terminateByIdsAsync(context);
    }

    @Nullable
    @Override
    public BatchData activateByIdsAsync(List<String> processInstancesIds) {
        return engineOperationServiceProvider.getProcessInstanceService().activateByIdsAsync(processInstancesIds);
    }

    @Nullable
    @Override
    public BatchData suspendByIdsAsync(List<String> processInstancesIds) {
        return engineOperationServiceProvider.getProcessInstanceService().suspendByIdsAsync(processInstancesIds);
    }

    @Override
    public long getCountByProcessDefinitionId(String processDefinitionId) {
        return engineOperationServiceProvider.getProcessInstanceService()
                .getCountByProcessDefinitionId(processDefinitionId);
    }

    @Override
    public long getCountByDeploymentId(String deploymentId) {
        return engineOperationServiceProvider.getProcessInstanceService().getCountByDeploymentId(deploymentId);
    }

    @Override
    public long getCountByProcessDefinitionKey(String processDefinitionKey) {
        return engineOperationServiceProvider.getProcessInstanceService()
                .getCountByProcessDefinitionKey(processDefinitionKey);
    }
}
