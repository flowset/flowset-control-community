/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.jmix.core.Metadata;
import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.entity.variable.ObjectTypeInfo;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.entity.variable.VariableValueInfo;
import org.camunda.community.rest.client.model.HistoricVariableInstanceDto;
import org.camunda.community.rest.client.model.VariableInstanceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class VariableMapper {


    @Autowired
    Metadata metadata;

    @Mapping(target = "historicVariableInstanceId", source = "id")
    @Mapping(target = "valueInfo", expression = "java(createValueInfo(source.getValueInfo()))")
    public abstract HistoricVariableInstanceData fromHistoricVariableInstanceDto(HistoricVariableInstanceDto source);

    HistoricVariableInstanceData targetClassFactory() {
        return metadata.create(HistoricVariableInstanceData.class);
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "variableInstanceId", source = "id")
    @Mapping(target = "valueInfo", expression = "java(createValueInfo(source.getValueInfo()))")
    public abstract VariableInstanceData fromVariableDto(VariableInstanceDto source);


    VariableInstanceData runtimeVariableTargetClassFactory() {
        return metadata.create(VariableInstanceData.class);
    }

    @Nullable
    public VariableValueInfo createValueInfo(Map<String, Object> valueInfoMap) {
        if (valueInfoMap != null && !valueInfoMap.isEmpty()) {
            VariableValueInfo variableValueInfo = metadata.create(VariableValueInfo.class);

            ObjectTypeInfo objectTypeInfo = metadata.create(ObjectTypeInfo.class);
            objectTypeInfo.setObjectTypeName((String) valueInfoMap.get("objectTypeName"));
            objectTypeInfo.setSerializationDataFormat((String) valueInfoMap.get("serializationDataFormat"));

            variableValueInfo.setObject(objectTypeInfo);

            variableValueInfo.setEncoding((String) valueInfoMap.get("encoding"));
            variableValueInfo.setFilename((String) valueInfoMap.get("filename"));
            variableValueInfo.setMimeType((String) valueInfoMap.get("mimeType"));
            return variableValueInfo;
        }
        return null;
    }

    Date map(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        Instant instant = value.toInstant();
        return Date.from(instant);
    }
}
