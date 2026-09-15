/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.jmix.core.Metadata;
import io.flowset.control.entity.UserTaskData;
import org.camunda.community.rest.client.model.HistoricTaskInstanceDto;
import org.camunda.community.rest.client.model.TaskWithAttachmentAndCommentDto;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

@Mapper(componentModel = "spring")
public abstract class TaskMapper {
    @Autowired
    Metadata metadata;

    @Mapping(target = "createTime", source = "startTime")
    @Mapping(target = "suspended", ignore = true)
    @Mapping(target = "processExecutionId", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "formKey", ignore = true)
    @Mapping(target = "delegationState", ignore = true)
    @Mapping(target = "taskId", source = "id")
    @Mapping(target = "dueDate", source = "due")
    @Mapping(target = "followUpDate", source = "followUp")
    public abstract UserTaskData fromTaskDto(HistoricTaskInstanceDto source);

    @Mapping(target = "rootProcessInstanceId", ignore = true)
    @Mapping(target = "removalTime", ignore = true)
    @Mapping(target = "processExecutionId", ignore = true)
    @Mapping(target = "processDefinitionKey", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "deleteReason", ignore = true)
    @Mapping(target = "caseDefinitionKey", ignore = true)
    @Mapping(target = "activityInstanceId", ignore = true)
    @Mapping(target = "taskId", source = "id")
    @Mapping(target = "dueDate", source = "due")
    @Mapping(target = "followUpDate", source = "followUp")
    @Mapping(target = "lastUpdateDate", source = "lastUpdated")
    @Mapping(target = "createTime", source = "created")
    @Mapping(target = "delegationState", expression = "java(delegationStateToString(source))")
    public abstract UserTaskData fromRuntimeTaskDto(TaskWithAttachmentAndCommentDto source);

    UserTaskData targetClassFactory() {
        return metadata.create(UserTaskData.class);
    }

    @Nullable
    String delegationStateToString(TaskWithAttachmentAndCommentDto source) {
        TaskWithAttachmentAndCommentDto.DelegationStateEnum delegationState = source.getDelegationState();

        return delegationState != null ? delegationState.getValue() : null;
    }

    Date map(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        Instant instant = value.toInstant();
        return Date.from(instant);
    }
}
