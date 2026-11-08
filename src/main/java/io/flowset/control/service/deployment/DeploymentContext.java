/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A context that contains the following options to deploy resources (processes, decisions, forms, etc.):
 * <ul>
 *     <li>Resource name: a filename containing a business process, e.g. approve-invoice-process.bpmn.</li>
 *     <li>Resource content: an input stream containing a business process described in BPMN 2.0 XML format.</li>
 *     <li>Resources: additional resources (name to content) to be deployed within the same deployment.
 *     Can be used together with the single resource set via {@link #withResource(String, InputStream)}.</li>
 *     <li>Deployment name: an optional name of the deployment.</li>
 *     <li>Skip unchanged resources: if {@code true}, duplicate filtering is enabled on a per-resource basis
 *     ({@code deploy-changed-only}), so a resource is deployed only if it differs from the latest deployed version
 *     of the resource with the same name (within previous deployments with the same deployment name and source).</li>
 * </ul>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DeploymentContext {
    private String resourceName;
    private InputStream resourceContent;
    private Map<String, InputStream> resources = new LinkedHashMap<>();
    private String deploymentName;
    private boolean skipUnchangedResources;

    /**
     * Sets name and content of the resource that should be deployed to the BPM engine.
     * @param resourceName a resource name, e.g. approve-invoice.bpmn
     * @param resourceContent an input stream containing resource content
     * @return current context
     */
    public DeploymentContext withResource(String resourceName, InputStream resourceContent) {
        this.resourceName = resourceName;
        this.resourceContent = resourceContent;

        return this;
    }

    /**
     * Adds a resource that should be deployed to the BPM engine within the deployment.
     * @param resourceName a resource name, e.g. approve-invoice.bpmn
     * @param resourceContent an input stream containing resource content
     * @return current context
     */
    public DeploymentContext addResource(String resourceName, InputStream resourceContent) {
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
