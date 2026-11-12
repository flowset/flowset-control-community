/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.incident.impl;

import io.flowset.control.dto.ActivityIncidentData;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.service.incident.IncidentLoadContext;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link IncidentService} calls when no BPM engine is selected.
 */
@EngineServiceDelegate
@Service("control_NoopIncidentService")
public class NoopIncidentServiceImpl implements IncidentService {

    @Override
    public List<ActivityIncidentData> findRuntimeIncidents(String processInstanceId) {
        return List.of();
    }

    @Override
    public List<IncidentData> findRuntimeIncidents(IncidentLoadContext loadContext) {
        return List.of();
    }

    @Nullable
    @Override
    public IncidentData findRuntimeIncidentById(String incidentId) {
        return null;
    }

    @Override
    public long getRuntimeIncidentCount(@Nullable IncidentFilter filter) {
        return 0;
    }

    @Override
    public List<HistoricIncidentData> findHistoricIncidents(IncidentLoadContext loadContext) {
        return List.of();
    }

    @Override
    public long getHistoricIncidentCount(@Nullable IncidentFilter filter) {
        return 0;
    }

    @Nullable
    @Override
    public HistoricIncidentData findHistoricIncidentById(String id) {
        return null;
    }
}
