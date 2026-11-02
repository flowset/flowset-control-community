/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.incident.impl;

import io.flowset.control.dto.ActivityIncidentData;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.incident.IncidentLoadContext;
import io.flowset.control.service.incident.IncidentService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link IncidentService} of the selected engine type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_IncidentService")
public class IncidentServiceImpl implements IncidentService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public IncidentServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<ActivityIncidentData> findRuntimeIncidents(String processInstanceId) {
        return engineOperationServiceProvider.getIncidentService().findRuntimeIncidents(processInstanceId);
    }

    @Override
    public List<IncidentData> findRuntimeIncidents(IncidentLoadContext loadContext) {
        return engineOperationServiceProvider.getIncidentService().findRuntimeIncidents(loadContext);
    }

    @Nullable
    @Override
    public IncidentData findRuntimeIncidentById(String incidentId) {
        return engineOperationServiceProvider.getIncidentService().findRuntimeIncidentById(incidentId);
    }

    @Override
    public long getRuntimeIncidentCount(@Nullable IncidentFilter filter) {
        return engineOperationServiceProvider.getIncidentService().getRuntimeIncidentCount(filter);
    }

    @Override
    public List<HistoricIncidentData> findHistoricIncidents(IncidentLoadContext loadContext) {
        return engineOperationServiceProvider.getIncidentService().findHistoricIncidents(loadContext);
    }

    @Override
    public long getHistoricIncidentCount(@Nullable IncidentFilter filter) {
        return engineOperationServiceProvider.getIncidentService().getHistoricIncidentCount(filter);
    }

    @Nullable
    @Override
    public HistoricIncidentData findHistoricIncidentById(String id) {
        return engineOperationServiceProvider.getIncidentService().findHistoricIncidentById(id);
    }
}
