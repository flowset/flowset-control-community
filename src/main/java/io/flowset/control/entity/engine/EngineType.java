/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.engine;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;


public enum EngineType implements EnumClass<String> {

    CAMUNDA_7("Camunda 7"),
    OPERATON("OPERATON"),
    FLUXNOVA("FLUXNOVA");

    private final String id;

    EngineType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EngineType fromId(String id) {
        for (EngineType at : EngineType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

}