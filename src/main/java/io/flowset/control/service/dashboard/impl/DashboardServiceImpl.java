/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.dashboard.impl;

import io.flowset.control.entity.ProcessExecutionGraphEntry;
import io.flowset.control.entity.dashboard.ProcessDefinitionStatistics;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.dashboard.DashboardService;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link DashboardService} of the given engine's type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_DashboardService")
public class DashboardServiceImpl implements DashboardService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public DashboardServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<ProcessExecutionGraphEntry> getRecentActivityStatistics(BpmEngine bpmEngine) {
        return engineOperationServiceProvider.getDashboardService(bpmEngine).getRecentActivityStatistics(bpmEngine);
    }

    @Override
    public long getUserTasksCount(BpmEngine bpmEngine) {
        return engineOperationServiceProvider.getDashboardService(bpmEngine).getUserTasksCount(bpmEngine);
    }

    @Override
    public long getDeployedProcessesCount(BpmEngine bpmEngine) {
        return engineOperationServiceProvider.getDashboardService(bpmEngine).getDeployedProcessesCount(bpmEngine);
    }

    @Override
    public long getRunningProcessCount(BpmEngine bpmEngine) {
        return engineOperationServiceProvider.getDashboardService(bpmEngine).getRunningProcessCount(bpmEngine);
    }

    @Override
    public long getSuspendedProcessCount(BpmEngine bpmEngine) {
        return engineOperationServiceProvider.getDashboardService(bpmEngine).getSuspendedProcessCount(bpmEngine);
    }

    @Override
    public List<ProcessDefinitionStatistics> getProcessDefinitionStatistics(BpmEngine bpmEngine) {
        return engineOperationServiceProvider.getDashboardService(bpmEngine)
                .getProcessDefinitionStatistics(bpmEngine);
    }
}
