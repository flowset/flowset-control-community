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
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link BatchService} calls when no BPM engine is selected: loads return nothing.
 */
@EngineServiceDelegate
@Service("control_NoopBatchService")
public class NoopBatchServiceImpl implements BatchService {

    @Override
    public List<BatchStatisticsData> findAllBatchStatistics(BatchLoadContext loadContext) {
        return List.of();
    }

    @Nullable
    @Override
    public BatchStatisticsData findBatchStatisticsById(String batchId) {
        return null;
    }

    @Nullable
    @Override
    public BatchData getById(String batchId) {
        return null;
    }

    @Nullable
    @Override
    public BatchData findHistoricBatchById(String batchId) {
        return null;
    }

    @Override
    public long getBatchStatisticsCount(@Nullable BatchFilter filter) {
        return 0;
    }

    @Override
    public List<BatchData> findAllHistoricBatches(BatchLoadContext loadContext) {
        return List.of();
    }

    @Override
    public long getHistoricBatchCount(@Nullable BatchFilter filter) {
        return 0;
    }
}
