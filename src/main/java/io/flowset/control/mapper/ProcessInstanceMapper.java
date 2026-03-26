/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.jmix.core.Metadata;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.rest.client.model.HistoricProcessInstanceDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class ProcessInstanceMapper {
    @Autowired
    Metadata metadata;

    @Mapping(target = "superProcessInstanceId", ignore = true)
    @Mapping(target = "hasIncidents", ignore = true)
    @Mapping(target = "instanceId", source = "id")
    @Mapping(target = "complete", constant = "false")
    @Mapping(target = "externallyTerminated", constant = "false")
    @Mapping(target = "internallyTerminated", constant = "false")
    @Mapping(target = "processDefinitionName", ignore = true)
    @Mapping(target = "processDefinitionKey", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "processDefinitionVersion", ignore = true)
    @Mapping(target = "deleteReason", ignore = true)
    public abstract ProcessInstanceData fromProcessInstanceModel(ProcessInstance source);

    @Mapping(target = "hasIncidents", ignore = true)
    @Mapping(target = "instanceId", source = "id")
    @Mapping(target = "complete", ignore = true)
    @Mapping(target = "suspended", ignore = true)
    @Mapping(target = "externallyTerminated", ignore = true)
    @Mapping(target = "internallyTerminated", ignore = true)
    @Mapping(target = "deleteReason", source = "deleteReason")
    public abstract ProcessInstanceData fromHistoryProcessInstanceDto(HistoricProcessInstanceDto source);

    @Nullable
    protected Date offsetDateTimeToDate(@Nullable OffsetDateTime offsetDateTime) {
        return Optional.ofNullable(offsetDateTime).map(value -> Date.from(value.toInstant())).orElse(null);
    }

    @AfterMapping
    protected void afterHistoricProcessInstanceDtoMapping(@MappingTarget ProcessInstanceData processInstanceData, HistoricProcessInstanceDto sourceDto) {
        HistoricProcessInstanceDto.StateEnum state = sourceDto.getState();
        switch (state) {
            case COMPLETED -> processInstanceData.setComplete(true);
            case SUSPENDED -> processInstanceData.setSuspended(true);
            case EXTERNALLY_TERMINATED -> processInstanceData.setExternallyTerminated(true);
            case INTERNALLY_TERMINATED -> processInstanceData.setInternallyTerminated(true);
            default -> {
            }
        }
    }

    @Mapping(target = "hasIncidents", ignore = true)
    @Mapping(target = "instanceId", source = "id")
    public abstract RuntimeProcessInstanceData toRuntimeProcessInstanceData(ProcessInstance source);

    public <T extends RuntimeProcessInstanceData> T createProcessInstanceData(@TargetType Class<T> processInstanceClass) {
        return metadata.create(processInstanceClass);
    }
}
