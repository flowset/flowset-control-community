/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.flowset.control.entity.variable.ObjectTypeInfo;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.entity.variable.VariableValueInfo;
import io.jmix.core.Metadata;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProcessVariableMapper {
    @Autowired
    Metadata metadata;

    public VariableInstanceData fromProcessKeyValueModel(Map.Entry<String, Object> stringObjectEntry) {
        VariableInstanceData variableInstance = metadata.create(VariableInstanceData.class);
        variableInstance.setName(stringObjectEntry.getKey());
        variableInstance.setValue(stringObjectEntry.getValue());
        return variableInstance;
    }

    public VariableInstanceData fromVariableDto(Map.Entry<String, VariableValueDto> dto) {
        VariableInstanceData variableInstance = metadata.create(VariableInstanceData.class);
        variableInstance.setName(dto.getKey());
        variableInstance.setValue(dto.getValue().getValue());
        variableInstance.setType(dto.getValue().getType());
        Map<String, Object> valueInfoMap = dto.getValue().getValueInfo();
        if (valueInfoMap != null && !valueInfoMap.isEmpty()) {
            VariableValueInfo variableValueInfo = metadata.create(VariableValueInfo.class);
            if (valueInfoMap.containsKey("object")) {
                ObjectTypeInfo objectTypeInfo = metadata.create(ObjectTypeInfo.class);
                objectTypeInfo.setObjectTypeName((String) ((Map) valueInfoMap.get("object")).get("objectTypeName"));
                objectTypeInfo.setSerializationDataFormat((String) ((Map) valueInfoMap.get("object")).get("serializationDataFormat"));
            }
            variableValueInfo.setEncoding((String) valueInfoMap.get("encoding"));
            variableValueInfo.setFilename((String) valueInfoMap.get("filename"));
            variableValueInfo.setMimeType((String) valueInfoMap.get("mimetype"));
        }
        return variableInstance;
    }
}
