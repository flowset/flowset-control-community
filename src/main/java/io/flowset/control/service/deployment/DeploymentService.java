/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment;

import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.deployment.DeploymentResource;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.security.SecuredEntityLoad;
import io.flowset.control.security.SecuredEntityOperation;
import io.jmix.core.security.EntityOp;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Provides methods to deploy processes to the BPM engine and get data about process deployment.
 */
public interface DeploymentService {

    /**
     * Deploys the business process to BPM engine using the specified context.
     *
     * @param context a context containing business process filename and content that should be deployed.
     * @return deployment result including information about deployed processes
     */
    DeploymentWithDefinitions createDeployment(DeploymentContext context);

    /**
     * Loads from the engine a process deployment data using the specified identifier.
     *
     * @param deploymentId a deployment identifier
     * @return found deployment information or null if not found
     */
    @Nullable
    @SecuredEntityLoad(entityClass = DeploymentData.class)
    DeploymentData findById(String deploymentId);

    /**
     * Loads deployments from the engine using the specified context.
     *
     * @param context a context to load deployments
     * @return a list of deployments
     */
    @SecuredEntityLoad(entityClass = DeploymentData.class)
    List<DeploymentData> findAll(DeploymentLoadContext context);

    /**
     * Loads from the engine the total count of deployments that match the specified filter.
     *
     * @param filter a filter to get count of deployments
     * @return a count of deployments
     */
    @SecuredEntityLoad(entityClass = DeploymentData.class)
    long getCount(@Nullable DeploymentFilter filter);

    /**
     * Loads all resource names for provided deployment id.
     *
     * @param deploymentId id of a deployment
     * @return a list of deployed resource names
     */
    @SecuredEntityLoad(entityClass = DeploymentResource.class)
    List<DeploymentResource> getDeploymentResources(String deploymentId);

    /**
     * Loads resource data for provided deployment and resource ids
     *
     * @param deploymentId id of the deployment
     * @param resourceId   id of the resource
     * @return resource with binary data
     */
    @SecuredEntityLoad(entityClass = DeploymentResource.class)
    Resource getDeploymentResourceData(String deploymentId, String resourceId);

    /**
     * Deletes a deployment.
     *
     * @param deploymentId              id of the deployment
     * @param deleteAllRelatedInstances remove process instances
     * @param skipCustomListeners       skip custom listeners
     * @param skipIoMappings            skip IO mappings
     */
    @SecuredEntityOperation(entityClass = DeploymentData.class, entityOp = EntityOp.DELETE)
    void deleteById(String deploymentId, boolean deleteAllRelatedInstances, boolean skipCustomListeners,
                    boolean skipIoMappings);
}
