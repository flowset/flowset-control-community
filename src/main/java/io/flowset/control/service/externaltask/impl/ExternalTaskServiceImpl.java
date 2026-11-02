/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.externaltask.impl;

import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.filter.ExternalTaskFilter;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.externaltask.ExternalTaskLoadContext;
import io.flowset.control.service.externaltask.ExternalTaskService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link ExternalTaskService} of the selected engine type, see
 * {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_ExternalTaskService")
public class ExternalTaskServiceImpl implements ExternalTaskService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public ExternalTaskServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<ExternalTaskData> findRunningTasks(ExternalTaskLoadContext loadContext) {
        return engineOperationServiceProvider.getExternalTaskService().findRunningTasks(loadContext);
    }

    @Nullable
    @Override
    public ExternalTaskData findById(String externalTaskId) {
        return engineOperationServiceProvider.getExternalTaskService().findById(externalTaskId);
    }

    @Override
    public long getRunningTasksCount(@Nullable ExternalTaskFilter filter) {
        return engineOperationServiceProvider.getExternalTaskService().getRunningTasksCount(filter);
    }

    @Override
    public void setRetries(String externalTaskId, int retries) {
        engineOperationServiceProvider.getExternalTaskService().setRetries(externalTaskId, retries);
    }

    @Nullable
    @Override
    public BatchData setRetriesAsync(List<String> externalTaskIds, int retries) {
        return engineOperationServiceProvider.getExternalTaskService().setRetriesAsync(externalTaskIds, retries);
    }

    @Override
    public String getErrorDetails(String externalTaskId) {
        return engineOperationServiceProvider.getExternalTaskService().getErrorDetails(externalTaskId);
    }
}
