/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.flowset.control.entity.activity.ActivityShortData;
import io.flowset.control.entity.activity.ActivityInstanceTreeItem;
import io.jmix.core.Metadata;
import io.flowset.control.entity.activity.ProcessActivityStatistics;
import io.flowset.control.entity.dashboard.IncidentStatistics;
import org.camunda.community.rest.client.model.ActivityInstanceDto;
import org.camunda.community.rest.client.model.ActivityStatisticsResultDto;
import org.camunda.community.rest.client.model.HistoricActivityInstanceDto;
import org.camunda.community.rest.client.model.IncidentStatisticsResultDto;
import org.camunda.community.rest.client.model.TransitionInstanceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

@Mapper(componentModel = "spring")
public abstract class ActivityMapper {

    @Autowired
    Metadata metadata;

    @Mapping(target = "internalId", source = "id")
    @Mapping(target = "id", ignore = true)
    public abstract ActivityShortData fromActivityDto(HistoricActivityInstanceDto source);

    @Mapping(target = "activityInstanceId", source = "id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentActivityInstance", ignore = true)
    @Mapping(target = "transition", constant = "false")
    public abstract ActivityInstanceTreeItem fromRuntimeActivityDto(ActivityInstanceDto source);

    ActivityInstanceTreeItem targetTreeItemClassFactory() {
        return metadata.create(ActivityInstanceTreeItem.class);
    }

    @Mapping(target = "activityInstanceId", source = "id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentActivityInstance", ignore = true)
    @Mapping(target = "transition", constant = "true")
    @Mapping(target = "executionIds", expression = "java(java.util.Collections.singletonList(source.getExecutionId()))")
    public abstract ActivityInstanceTreeItem fromRuntimeTransitionDto(TransitionInstanceDto source);

    ActivityShortData targetClassFactory() {
        return metadata.create(ActivityShortData.class);
    }

    Date map(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        Instant instant = value.toInstant();
        return Date.from(instant);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instanceCount", source = "instances")
    @Mapping(target = "failedJobCount", source = "failedJobs")
    @Mapping(target = "activityId", source = "id")
    public abstract ProcessActivityStatistics fromActivityStatisticsResult(ActivityStatisticsResultDto source);

    ProcessActivityStatistics activityStatisticsClassFactory() {
        return metadata.create(ProcessActivityStatistics.class);
    }

    @Mapping(target = "id", ignore = true)
    public abstract IncidentStatistics fromStatisticsResultDto(IncidentStatisticsResultDto resultDto);

    IncidentStatistics incidentStatisticsClassFactory() {
        return metadata.create(IncidentStatistics.class);
    }
}
