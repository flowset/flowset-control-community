/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.incident.IncidentData;
import io.jmix.core.Metadata;
import org.camunda.community.rest.client.model.HistoricIncidentDto;
import org.camunda.community.rest.client.model.IncidentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class IncidentMapper {
    @Autowired
    Metadata metadata;

    @Mapping(target = "type", source = "incidentType")
    @Mapping(target = "message", source = "incidentMessage")
    @Mapping(target = "incidentId", source = "id")
    @Mapping(target = "timestamp", source = "incidentTimestamp")
    public abstract IncidentData fromIncidentModel(IncidentDto source);

    IncidentData targetClassFactory() {
        return metadata.create(IncidentData.class);
    }

    @Mapping(target = "type", source = "incidentType")
    @Mapping(target = "message", source = "incidentMessage")
    @Mapping(target = "incidentId", source = "id")
    public abstract HistoricIncidentData fromHistoricIncidentModel(HistoricIncidentDto historicIncidentDto);

    HistoricIncidentData historicIncidentTargetClassFactory() {
        return metadata.create(HistoricIncidentData.class);
    }
}
