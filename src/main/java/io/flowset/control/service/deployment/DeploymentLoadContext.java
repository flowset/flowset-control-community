/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment;

import io.jmix.core.Sort;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.service.ItemListLoadContext;

/**
 * A context that contains the following options to load incidents:
 * <ul>
 *     <li>Pagination options: first and max results</li>
 *     <li>Sort options: property and direction to sort. Supported properties: "deploymentId" (or "id"), "name",
 *     "deploymentTime" (or "time"), "tenantId". Other properties are ignored.</li>
 *     <li>Filtering options</li>
 * </ul>
 * <p>
 * Example: load first 50 deployments considering latest versions only and sort them by name
 * <pre>
 *     DeploymentFilter filter = metadata.create(DeploymentFilter.class);
 *     filter.setLatestVersionOnly(true);
 *
 *     DeploymentLoadContext context = newDeploymentLoadContext()
 *      .setFirstResult(0)
 *      .setMaxResults(50)
 *      .setFilter(filter)
 *      .setSort(Sort.by(Sort.Direction.ASC, "name"));
 * </pre>
 *
 * @see DeploymentFilter
 * @see io.jmix.core.Metadata
 * @see Sort
 */
public class DeploymentLoadContext extends ItemListLoadContext<DeploymentFilter> {

    /**
     * Sets a process definition filter.
     *
     * @param filter an instance of {@link DeploymentFilter}
     * @return current context
     */
    public DeploymentLoadContext setFilter(DeploymentFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets a first result number.
     *
     * @param firstResult first result
     * @return current context
     */
    public DeploymentLoadContext setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * Sets a max number of results.
     *
     * @param maxResults max results
     * @return current context
     */
    public DeploymentLoadContext setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Sets sort options.
     *
     * @param sort sort options
     * @return current context
     */
    public DeploymentLoadContext setSort(Sort sort) {
        this.sort = sort;
        return this;
    }
}
