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
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link ActivityService} calls when no BPM engine is selected.
 */
@EngineServiceDelegate
@Service("control_NoopActivityService")
public class NoopActivityServiceImpl implements ActivityService {

    @Override
    public List<ActivityShortData> findRunningActivities(String processInstanceId) {
        return List.of();
    }

    @Override
    public List<ActivityShortData> findFinishedActivities(String processInstanceId) {
        return List.of();
    }

    @Override
    public List<ActivityInstanceTreeItem> getActivityInstancesTree(String processInstanceId) {
        return List.of();
    }

    @Override
    public List<HistoricActivityInstanceData> findAllHistoryActivities(ActivityLoadContext loadContext) {
        return List.of();
    }

    @Override
    public long getHistoryActivitiesCount(@Nullable ActivityFilter filter) {
        return 0;
    }

    @Nullable
    @Override
    public HistoricActivityInstanceData findById(String activityInstanceId) {
        return null;
    }

    @Override
    public List<ProcessActivityStatistics> getStatisticsByProcessId(String processDefinitionId) {
        return List.of();
    }
}
