/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class VariableValueDto {
    private String type;
    private Object value;
    private ValueInfo valueInfo;

    public VariableValueDto(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ValueInfo {
        private String objectTypeName;
        private String serializationDataFormat;

        private String filename;
        private String mimetype;
        private String encoding;
    }
}
