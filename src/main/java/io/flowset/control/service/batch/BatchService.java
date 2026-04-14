/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.batch;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.BatchFilter;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Provides methods to load batch and batch statistics from the BPM engine.
 */
public interface BatchService {

    /**
     * Loads batch statistics from the engine using the specified context.
     *
     * @param loadContext a context to load batch statistics
     * @return a list of batch statistics
     */
    List<BatchStatisticsData> findAllBatchStatistics(BatchLoadContext loadContext);

    /**
     * Loads a batch statistics by batch id.
     *
     * @param batchId batch id
     * @return found batch statistics or {@code null} if not found
     */
    @Nullable
    BatchStatisticsData findBatchStatisticsById(String batchId);

    /**
     * Loads batch by specified id.
     * First tries to find a runtime batch, and if no runtime batch is found, tries to find a historic batch.
     *
     * @param batchId batch id
     * @return found batch data or {@code null} if not found
     */
    @Nullable
    BatchData getById(String batchId);

    /**
     * Loads historic batch by specified id.
     *
     * @param batchId batch id
     * @return found historic batch or {@code null} if not found
     */
    @Nullable
    BatchData findHistoricBatchById(String batchId);

    /**
     * Loads from engine the total count of batch statistics that match the specified filter.
     *
     * @param filter a batch filter instance
     * @return count of batch statistics
     */
    long getBatchStatisticsCount(@Nullable BatchFilter filter);

    /**
     * Loads historic batches from the engine using the specified context.
     *
     * @param loadContext a context to load historic batches
     * @return a list of historic batches
     */
    List<BatchData> findAllHistoricBatches(BatchLoadContext loadContext);

    /**
     * Loads from engine the total count of historic batches that match the specified filter.
     *
     * @param filter a historic batch filter instance
     * @return count of historic batches
     */
    long getHistoricBatchCount(@Nullable BatchFilter filter);

}
