/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.impl;

import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.deployment.DeploymentResource;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.service.deployment.DeploymentContext;
import io.flowset.control.service.deployment.DeploymentLoadContext;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link DeploymentService} of the selected engine type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_DeploymentService")
public class DeploymentServiceImpl implements DeploymentService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public DeploymentServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public DeploymentWithDefinitions createDeployment(DeploymentContext context) {
        return engineOperationServiceProvider.getDeploymentService().createDeployment(context);
    }

    @Nullable
    @Override
    public DeploymentData findById(String deploymentId) {
        return engineOperationServiceProvider.getDeploymentService().findById(deploymentId);
    }

    @Override
    public List<DeploymentData> findAll(DeploymentLoadContext context) {
        return engineOperationServiceProvider.getDeploymentService().findAll(context);
    }

    @Override
    public long getCount(@Nullable DeploymentFilter filter) {
        return engineOperationServiceProvider.getDeploymentService().getCount(filter);
    }

    @Override
    public List<DeploymentResource> getDeploymentResources(String deploymentId) {
        return engineOperationServiceProvider.getDeploymentService().getDeploymentResources(deploymentId);
    }

    @Override
    public Resource getDeploymentResourceData(String deploymentId, String resourceId) {
        return engineOperationServiceProvider.getDeploymentService()
                .getDeploymentResourceData(deploymentId, resourceId);
    }

    @Override
    public void deleteById(String deploymentId, boolean deleteAllRelatedInstances, boolean skipCustomListeners,
                           boolean skipIoMappings) {
        engineOperationServiceProvider.getDeploymentService()
                .deleteById(deploymentId, deleteAllRelatedInstances, skipCustomListeners, skipIoMappings);
    }
}
