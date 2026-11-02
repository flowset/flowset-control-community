/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.impl;

import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.deployment.DeploymentResource;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.service.deployment.DeploymentContext;
import io.flowset.control.service.deployment.DeploymentLoadContext;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link DeploymentService} calls when no BPM engine is selected: loads return nothing, operations throw
 * {@link EngineNotSelectedException}.
 */
@EngineServiceDelegate
@Service("control_NoopDeploymentService")
public class NoopDeploymentServiceImpl implements DeploymentService {

    @Override
    public DeploymentWithDefinitions createDeployment(DeploymentContext context) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Nullable
    @Override
    public DeploymentData findById(String deploymentId) {
        return null;
    }

    @Override
    public List<DeploymentData> findAll(DeploymentLoadContext context) {
        return List.of();
    }

    @Override
    public long getCount(@Nullable DeploymentFilter filter) {
        return 0;
    }

    @Override
    public List<DeploymentResource> getDeploymentResources(String deploymentId) {
        return List.of();
    }

    @Nullable
    @Override
    public Resource getDeploymentResourceData(String deploymentId, String resourceId) {
        return null;
    }

    @Override
    public void deleteById(String deploymentId, boolean deleteAllRelatedInstances, boolean skipCustomListeners,
                           boolean skipIoMappings) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }
}
