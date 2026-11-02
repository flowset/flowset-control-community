/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.job.impl;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobDefinitionData;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.job.JobLoadContext;
import io.flowset.control.service.job.JobService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link JobService} of the selected engine type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_JobService")
public class JobServiceImpl implements JobService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public JobServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<JobData> findAll(JobLoadContext loadContext) {
        return engineOperationServiceProvider.getJobService().findAll(loadContext);
    }

    @Override
    public long getCount(@Nullable JobFilter filter) {
        return engineOperationServiceProvider.getJobService().getCount(filter);
    }

    @Override
    public JobData findById(String jobId) {
        return engineOperationServiceProvider.getJobService().findById(jobId);
    }

    @Nullable
    @Override
    public JobDefinitionData findJobDefinition(String jobDefinitionId) {
        return engineOperationServiceProvider.getJobService().findJobDefinition(jobDefinitionId);
    }

    @Override
    public void setJobRetries(String jobId, int retries) {
        engineOperationServiceProvider.getJobService().setJobRetries(jobId, retries);
    }

    @Nullable
    @Override
    public BatchData setJobRetriesAsync(List<String> jobIds, int retries) {
        return engineOperationServiceProvider.getJobService().setJobRetriesAsync(jobIds, retries);
    }

    @Override
    public String getErrorDetails(String jobId) {
        return engineOperationServiceProvider.getJobService().getErrorDetails(jobId);
    }

    @Override
    public void activateJob(String jobId) {
        engineOperationServiceProvider.getJobService().activateJob(jobId);
    }

    @Override
    public void suspendJob(String jobId) {
        engineOperationServiceProvider.getJobService().suspendJob(jobId);
    }
}
