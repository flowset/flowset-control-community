/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.engine;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;


public enum EngineType implements EnumClass<String> {

    CAMUNDA_7("Camunda 7", EngineFamily.CAMUNDA_7),
    OPERATON("OPERATON", EngineFamily.CAMUNDA_7),
    FLUXNOVA("FLUXNOVA", EngineFamily.CAMUNDA_7),
    CIB_SEVEN("CIB_SEVEN", EngineFamily.CAMUNDA_7);

    private final String id;
    private final EngineFamily family;

    EngineType(String id, @Nullable EngineFamily family) {
        this.id = id;
        this.family = family;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public EngineFamily getFamily() {
        return family;
    }

    public boolean isCamunda7Compatible() {
        return family == EngineFamily.CAMUNDA_7;
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