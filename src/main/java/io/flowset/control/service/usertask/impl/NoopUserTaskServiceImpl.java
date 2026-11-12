/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.usertask.impl;

import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.filter.UserTaskFilter;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.service.usertask.UserTaskLoadContext;
import io.flowset.control.service.usertask.UserTaskService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Serves {@link UserTaskService} calls when no BPM engine is selected: loads return nothing, operations throw
 * {@link EngineNotSelectedException}.
 */
@EngineServiceDelegate
@Service("control_NoopUserTaskService")
public class NoopUserTaskServiceImpl implements UserTaskService {

    @Override
    public List<UserTaskData> findRuntimeTasks(UserTaskLoadContext loadContext) {
        return List.of();
    }

    @Override
    public long getRuntimeTasksCount(@Nullable UserTaskFilter filter) {
        return 0;
    }

    @Override
    public List<UserTaskData> findHistoricTasks(UserTaskLoadContext loadContext) {
        return List.of();
    }

    @Override
    public long getHistoryTasksCount(@Nullable UserTaskFilter filter) {
        return 0;
    }

    @Override
    public void setAssignee(String taskId, String newAssignee) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void completeTaskById(String taskId, Collection<VariableInstanceData> variableInstances) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Nullable
    @Override
    public UserTaskData findTaskById(String taskId) {
        return null;
    }
}
