/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.dashboard.impl;

import io.flowset.control.entity.ProcessExecutionGraphEntry;
import io.flowset.control.entity.dashboard.ProcessDefinitionStatistics;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.dashboard.DashboardService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link DashboardService} calls when no BPM engine is given.
 */
@EngineServiceDelegate
@Service("control_NoopDashboardService")
public class NoopDashboardServiceImpl implements DashboardService {

    @Override
    public List<ProcessExecutionGraphEntry> getRecentActivityStatistics(BpmEngine bpmEngine) {
        return List.of();
    }

    @Override
    public long getUserTasksCount(BpmEngine bpmEngine) {
        return 0;
    }

    @Override
    public long getDeployedProcessesCount(BpmEngine bpmEngine) {
        return 0;
    }

    @Override
    public long getRunningProcessCount(BpmEngine bpmEngine) {
        return 0;
    }

    @Override
    public long getSuspendedProcessCount(BpmEngine bpmEngine) {
        return 0;
    }

    @Override
    public List<ProcessDefinitionStatistics> getProcessDefinitionStatistics(BpmEngine bpmEngine) {
        return List.of();
    }
}
