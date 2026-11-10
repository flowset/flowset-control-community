/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.batch.impl;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.service.batch.BatchLoadContext;
import io.flowset.control.service.batch.BatchService;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link BatchService} of the selected engine type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_BatchService")
public class BatchServiceImpl implements BatchService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public BatchServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<BatchStatisticsData> findAllBatchStatistics(BatchLoadContext loadContext) {
        return engineOperationServiceProvider.getBatchService().findAllBatchStatistics(loadContext);
    }

    @Nullable
    @Override
    public BatchStatisticsData findBatchStatisticsById(String batchId) {
        return engineOperationServiceProvider.getBatchService().findBatchStatisticsById(batchId);
    }

    @Nullable
    @Override
    public BatchData getById(String batchId) {
        return engineOperationServiceProvider.getBatchService().getById(batchId);
    }

    @Nullable
    @Override
    public BatchData findHistoricBatchById(String batchId) {
        return engineOperationServiceProvider.getBatchService().findHistoricBatchById(batchId);
    }

    @Override
    public long getBatchStatisticsCount(@Nullable BatchFilter filter) {
        return engineOperationServiceProvider.getBatchService().getBatchStatisticsCount(filter);
    }

    @Override
    public List<BatchData> findAllHistoricBatches(BatchLoadContext loadContext) {
        return engineOperationServiceProvider.getBatchService().findAllHistoricBatches(loadContext);
    }

    @Override
    public long getHistoricBatchCount(@Nullable BatchFilter filter) {
        return engineOperationServiceProvider.getBatchService().getHistoricBatchCount(filter);
    }
}
