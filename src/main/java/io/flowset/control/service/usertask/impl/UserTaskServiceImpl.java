/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.usertask.impl;

import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.filter.UserTaskFilter;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.usertask.UserTaskLoadContext;
import io.flowset.control.service.usertask.UserTaskService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Delegates to the {@link UserTaskService} of the selected engine type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_UserTaskService")
public class UserTaskServiceImpl implements UserTaskService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public UserTaskServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<UserTaskData> findRuntimeTasks(UserTaskLoadContext loadContext) {
        return engineOperationServiceProvider.getUserTaskService().findRuntimeTasks(loadContext);
    }

    @Override
    public long getRuntimeTasksCount(@Nullable UserTaskFilter filter) {
        return engineOperationServiceProvider.getUserTaskService().getRuntimeTasksCount(filter);
    }

    @Override
    public List<UserTaskData> findHistoricTasks(UserTaskLoadContext loadContext) {
        return engineOperationServiceProvider.getUserTaskService().findHistoricTasks(loadContext);
    }

    @Override
    public long getHistoryTasksCount(@Nullable UserTaskFilter filter) {
        return engineOperationServiceProvider.getUserTaskService().getHistoryTasksCount(filter);
    }

    @Override
    public void setAssignee(String taskId, String newAssignee) {
        engineOperationServiceProvider.getUserTaskService().setAssignee(taskId, newAssignee);
    }

    @Override
    public void completeTaskById(String taskId, Collection<VariableInstanceData> variableInstances) {
        engineOperationServiceProvider.getUserTaskService().completeTaskById(taskId, variableInstances);
    }

    @Nullable
    @Override
    public UserTaskData findTaskById(String taskId) {
        return engineOperationServiceProvider.getUserTaskService().findTaskById(taskId);
    }
}
