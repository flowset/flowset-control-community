/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.activity.impl;

import io.flowset.control.entity.activity.ActivityInstanceTreeItem;
import io.flowset.control.entity.activity.ActivityShortData;
import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.flowset.control.entity.activity.ProcessActivityStatistics;
import io.flowset.control.entity.filter.ActivityFilter;
import io.flowset.control.service.activity.ActivityLoadContext;
import io.flowset.control.service.activity.ActivityService;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link ActivityService} of the selected engine type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_ActivityService")
public class ActivityServiceImpl implements ActivityService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public ActivityServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<ActivityShortData> findRunningActivities(String processInstanceId) {
        return engineOperationServiceProvider.getActivityService().findRunningActivities(processInstanceId);
    }

    @Override
    public List<ActivityShortData> findFinishedActivities(String processInstanceId) {
        return engineOperationServiceProvider.getActivityService().findFinishedActivities(processInstanceId);
    }

    @Override
    public List<ActivityInstanceTreeItem> getActivityInstancesTree(String processInstanceId) {
        return engineOperationServiceProvider.getActivityService().getActivityInstancesTree(processInstanceId);
    }

    @Override
    public List<HistoricActivityInstanceData> findAllHistoryActivities(ActivityLoadContext loadContext) {
        return engineOperationServiceProvider.getActivityService().findAllHistoryActivities(loadContext);
    }

    @Override
    public long getHistoryActivitiesCount(@Nullable ActivityFilter filter) {
        return engineOperationServiceProvider.getActivityService().getHistoryActivitiesCount(filter);
    }

    @Nullable
    @Override
    public HistoricActivityInstanceData findById(String activityInstanceId) {
        return engineOperationServiceProvider.getActivityService().findById(activityInstanceId);
    }

    @Override
    public List<ProcessActivityStatistics> getStatisticsByProcessId(String processDefinitionId) {
        return engineOperationServiceProvider.getActivityService().getStatisticsByProcessId(processDefinitionId);
    }
}
