/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.variable;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;


public enum CamundaVariableType implements EnumClass<String> {

    STRING("String", true),
    NULL("Null", true),
    INTEGER("Integer", true),
    SHORT("Short", true),
    LONG("Long", true),
    DOUBLE("Double", true),
    BOOLEAN("Boolean", true),
    DATE("Date", true),
    OBJECT("Object", false),
    FILE("File", false),
    BYTES("Bytes", false);

    private final String id;
    private final boolean primitive;

    CamundaVariableType(String id, boolean primitive) {
        this.id = id;
        this.primitive = primitive;
    }

    public String getId() {
        return id;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    @Nullable
    public static CamundaVariableType fromId(String id) {
        for (CamundaVariableType at : CamundaVariableType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}