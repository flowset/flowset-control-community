/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.jmix.core.Metadata;
import io.flowset.control.entity.ExternalTaskData;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.community.rest.client.model.ExternalTaskDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

@Mapper(componentModel = "spring")
public abstract class ExternalTaskMapper {
    @Autowired
    Metadata metadata;

    @Mapping(target = "externalTaskId", source = "id")
    public abstract ExternalTaskData fromExternalTask(ExternalTask source);

    ExternalTaskData targetClassFactory() {
        return metadata.create(ExternalTaskData.class);
    }

    @Mapping(target = "externalTaskId", source = "id")
    public abstract ExternalTaskData fromExternalTaskDto(ExternalTaskDto source);

    @Nullable
    Date map(@Nullable OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        Instant instant = value.toInstant();
        return Date.from(instant);
    }
}
