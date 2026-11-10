/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.job.impl;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobDefinitionData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.service.job.JobLoadContext;
import io.flowset.control.service.job.JobService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link JobService} calls when no BPM engine is selected: loads return nothing, operations throw
 * {@link EngineNotSelectedException}.
 */
@EngineServiceDelegate
@Service("control_NoopJobService")
public class NoopJobServiceImpl implements JobService {

    @Override
    public List<JobData> findAll(JobLoadContext loadContext) {
        return List.of();
    }

    @Override
    public long getCount(@Nullable JobFilter filter) {
        return 0;
    }

    @Nullable
    @Override
    public JobData findById(String jobId) {
        return null;
    }

    @Nullable
    @Override
    public JobDefinitionData findJobDefinition(String jobDefinitionId) {
        return null;
    }

    @Override
    public void setJobRetries(String jobId, int retries) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public BatchData setJobRetriesAsync(List<String> jobIds, int retries) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Nullable
    @Override
    public String getErrorDetails(String jobId) {
        return null;
    }

    @Override
    public void activateJob(String jobId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void suspendJob(String jobId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }
}
