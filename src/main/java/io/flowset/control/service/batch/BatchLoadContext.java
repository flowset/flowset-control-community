/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.batch;

import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.service.ItemListLoadContext;
import io.jmix.core.Sort;

/**
 * A context that contains the following options to load batch statistics:
 * <ul>
 *     <li>Pagination options: first and max results</li>
 *     <li>Sort options: property and direction to sort. Supported properties: "id", "type", "tenantId", "startTime".</li>
 *     <li>Filtering options</li>
 * </ul>
 */
public class BatchLoadContext extends ItemListLoadContext<BatchFilter> {

    public BatchLoadContext setFilter(BatchFilter filter) {
        this.filter = filter;
        return this;
    }

    public BatchLoadContext setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    public BatchLoadContext setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public BatchLoadContext setSort(Sort sort) {
        this.sort = sort;
        return this;
    }
}
