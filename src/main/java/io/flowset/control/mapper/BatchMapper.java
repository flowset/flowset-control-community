/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.jmix.core.Metadata;
import org.camunda.community.rest.client.model.BatchDto;
import org.camunda.community.rest.client.model.BatchStatisticsDto;
import org.camunda.community.rest.client.model.HistoricBatchDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class BatchMapper {

    @Autowired
    Metadata metadata;

    @Mapping(target = "removalTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    public abstract BatchData fromBatchDto(BatchDto source);

    @Mapping(target = "suspended", ignore = true)
    @Mapping(target = "jobsCreated", ignore = true)
    public abstract BatchData fromHistoricBatchDto(HistoricBatchDto source);

    public abstract BatchStatisticsData fromBatchStatisticsDto(BatchStatisticsDto source);

    BatchData batchTargetClassFactory() {
        return metadata.create(BatchData.class);
    }

    BatchStatisticsData  batchStatisticsTargetClassFactory() {
        return metadata.create(BatchStatisticsData.class);
    }
}
