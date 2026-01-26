/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.jmix.core.Metadata;
import org.camunda.community.rest.client.model.HistoricActivityInstanceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
@Mapper(componentModel = "spring")
public abstract class HistoryActivityMapper {
    @Autowired
    Metadata metadata;

    @Mapping(target = "activityInstanceId", source = "id")
    @Mapping(target = "cancelled", source = "canceled")
    public abstract HistoricActivityInstanceData fromHistoryActivityDto(HistoricActivityInstanceDto source);

    HistoricActivityInstanceData targetClassFactory() {
        return metadata.create(HistoricActivityInstanceData.class);
    }

    Date map(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        Instant instant = value.toInstant();
        return Date.from(instant);
    }
}