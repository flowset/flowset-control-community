/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition.impl;

import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link ProcessDefinitionService} calls when no BPM engine is selected: loads return nothing, operations
 * throw {@link EngineNotSelectedException}.
 */
@EngineServiceDelegate
@Service("control_NoopProcessDefinitionService")
public class NoopProcessDefinitionServiceImpl implements ProcessDefinitionService {

    @Override
    public List<ProcessDefinitionData> findLatestVersions() {
        return List.of();
    }

    @Override
    public List<ProcessDefinitionData> findAll(ProcessDefinitionLoadContext context) {
        return List.of();
    }

    @Override
    public long getCount(@Nullable ProcessDefinitionFilter filter) {
        return 0;
    }

    @Override
    public List<ProcessDefinitionData> findAllByKey(String processDefinitionKey) {
        return List.of();
    }

    @Nullable
    @Override
    public ProcessDefinitionData getById(String processDefinitionId) {
        return null;
    }

    @Nullable
    @Override
    public String getBpmnXml(String processDefinitionId) {
        return null;
    }

    @Override
    public void activateById(String processDefinitionId, boolean activateBelongsInstances) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void activateAllVersionsByKey(String processDefinitionKey, boolean activateBelongsInstances) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void suspendById(String processDefinitionId, boolean suspendBelongsInstances) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void suspendAllVersionsByKey(String processDefinitionKey, boolean suspendBelongsInstances) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void deleteAllVersionsByKey(String processDefinitionKey, boolean deleteAllRelatedInstances) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void deleteById(String processDefinitionId, boolean deleteAllRelatedInstances) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }
}
