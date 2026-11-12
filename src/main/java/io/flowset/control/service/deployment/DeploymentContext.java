/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment;

import lombok.Getter;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A context that contains the following options to deploy resources (processes, decisions, forms, etc.):
 * <ul>
 *     <li>Resources: resource name (e.g. approve-invoice-process.bpmn) to content, deployed within one deployment.</li>
 *     <li>Deployment name: an optional name of the deployment.</li>
 *     <li>Skip unchanged resources: if {@code true}, duplicate filtering is enabled on a per-resource basis
 *     ({@code deploy-changed-only}), so a resource is deployed only if it differs from the latest deployed version
 *     of the resource with the same name (within previous deployments with the same deployment name and source).</li>
 * </ul>
 */
@Getter
public class DeploymentContext {
    private final Map<String, InputStream> resources = new LinkedHashMap<>();
    private String deploymentName;
    private boolean skipUnchangedResources;

    /**
     * Adds a resource that should be deployed to the BPM engine within the deployment.
     * @param resourceName a resource name, e.g. approve-invoice.bpmn
     * @param resourceContent an input stream containing resource content
     * @return current context
     */
    public DeploymentContext withResource(String resourceName, InputStream resourceContent) {
        this.resources.put(resourceName, resourceContent);

        return this;
    }

    /**
     * Sets a name of the deployment.
     * @param deploymentName a deployment name
     * @return current context
     */
    public DeploymentContext withDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;

        return this;
    }

    /**
     * Sets whether unchanged resources should be skipped (duplicate filtering on a per-resource basis):
     * a resource is deployed only if it differs from the latest deployed version of the resource with the same name.
     * @param skipUnchangedResources true to enable duplicate filtering
     * @return current context
     */
    public DeploymentContext withSkipUnchangedResources(boolean skipUnchangedResources) {
        this.skipUnchangedResources = skipUnchangedResources;

        return this;
    }
}
