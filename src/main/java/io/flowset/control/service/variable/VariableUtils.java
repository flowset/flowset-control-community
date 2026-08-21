/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable;

import io.flowset.control.entity.variable.CamundaVariableType;
import io.flowset.control.entity.variable.VariableInstanceData;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.Date;

public class VariableUtils {
    private static final DateTimeFormatter DATE_VALUE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .parseLenient()
            .appendOffset("+HHmm", "")
            .parseStrict()
            .toFormatter();


    public static VariableMap createVariableMap(Collection<VariableInstanceData> variableInstances) {
        VariableMap variables = Variables.createVariables();
        if (variableInstances != null) {
            for (VariableInstanceData variableInstanceData : variableInstances) {
                String variableName = variableInstanceData.getName();
                TypedValue typedValue = createPrimitiveTypedValue(variableInstanceData);
                if (variableName != null) {
                    variables.put(variableName, typedValue);
                }
            }
        }
        return variables;
    }

    public static TypedValue createPrimitiveTypedValue(VariableInstanceData variableInstanceData) {
        Object value = variableInstanceData.getValue();
        CamundaVariableType camundaVariableType = CamundaVariableType.fromId(variableInstanceData.getType());

        if (camundaVariableType == null) {
            return Variables.untypedValue(value);
        }

        TypedValue typedValue;
        switch (camundaVariableType) {
            case STRING -> typedValue = Variables.stringValue((String) value);
            case LONG -> typedValue = Variables.longValue((Long) value);
            case SHORT -> typedValue = Variables.shortValue((Short) value);
            case DOUBLE -> typedValue = Variables.doubleValue((Double) value);
            case BOOLEAN -> typedValue = Variables.booleanValue((Boolean) value);
            case INTEGER -> typedValue = Variables.integerValue((Integer) value);
            case DATE -> typedValue = Variables.dateValue((Date) value);
            case NULL -> typedValue = Variables.untypedNullValue();
            default -> typedValue = Variables.untypedValue(value);
        }
        return typedValue;
    }

    @Nullable
    public static Object parseDateValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String dateString) {
            OffsetDateTime dateTime = OffsetDateTime.parse(dateString, DATE_VALUE_FORMATTER);
            return Date.from(dateTime.toInstant());
        }
        return value;
    }
}
