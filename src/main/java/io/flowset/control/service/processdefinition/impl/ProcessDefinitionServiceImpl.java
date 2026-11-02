/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition.impl;

import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link ProcessDefinitionService} of the selected engine type, see
 * {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_ProcessDefinitionService")
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public ProcessDefinitionServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<ProcessDefinitionData> findLatestVersions() {
        return engineOperationServiceProvider.getProcessDefinitionService().findLatestVersions();
    }

    @Override
    public List<ProcessDefinitionData> findAll(ProcessDefinitionLoadContext context) {
        return engineOperationServiceProvider.getProcessDefinitionService().findAll(context);
    }

    @Override
    public long getCount(@Nullable ProcessDefinitionFilter filter) {
        return engineOperationServiceProvider.getProcessDefinitionService().getCount(filter);
    }

    @Override
    public List<ProcessDefinitionData> findAllByKey(String processDefinitionKey) {
        return engineOperationServiceProvider.getProcessDefinitionService().findAllByKey(processDefinitionKey);
    }

    @Nullable
    @Override
    public ProcessDefinitionData getById(String processDefinitionId) {
        return engineOperationServiceProvider.getProcessDefinitionService().getById(processDefinitionId);
    }

    @Override
    public String getBpmnXml(String processDefinitionId) {
        return engineOperationServiceProvider.getProcessDefinitionService().getBpmnXml(processDefinitionId);
    }

    @Override
    public void activateById(String processDefinitionId, boolean activateBelongsInstances) {
        engineOperationServiceProvider.getProcessDefinitionService()
                .activateById(processDefinitionId, activateBelongsInstances);
    }

    @Override
    public void activateAllVersionsByKey(String processDefinitionKey, boolean activateBelongsInstances) {
        engineOperationServiceProvider.getProcessDefinitionService()
                .activateAllVersionsByKey(processDefinitionKey, activateBelongsInstances);
    }

    @Override
    public void suspendById(String processDefinitionId, boolean suspendBelongsInstances) {
        engineOperationServiceProvider.getProcessDefinitionService()
                .suspendById(processDefinitionId, suspendBelongsInstances);
    }

    @Override
    public void suspendAllVersionsByKey(String processDefinitionKey, boolean suspendBelongsInstances) {
        engineOperationServiceProvider.getProcessDefinitionService()
                .suspendAllVersionsByKey(processDefinitionKey, suspendBelongsInstances);
    }

    @Override
    public void deleteAllVersionsByKey(String processDefinitionKey, boolean deleteAllRelatedInstances) {
        engineOperationServiceProvider.getProcessDefinitionService()
                .deleteAllVersionsByKey(processDefinitionKey, deleteAllRelatedInstances);
    }

    @Override
    public void deleteById(String processDefinitionId, boolean deleteAllRelatedInstances) {
        engineOperationServiceProvider.getProcessDefinitionService()
                .deleteById(processDefinitionId, deleteAllRelatedInstances);
    }
}
