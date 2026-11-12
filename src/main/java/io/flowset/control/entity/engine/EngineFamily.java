/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.engine;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

/**
 * Engine types that share one code base: an engine and its forks.
 *
 * @see EngineType#getFamily()
 */
public enum EngineFamily implements EnumClass<String> {

    /**
     * Camunda 7 and its forks: Operaton, Fluxnova, CIB seven.
     */
    CAMUNDA_7("CAMUNDA_7");

    private final String id;

    EngineFamily(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EngineFamily fromId(String id) {
        for (EngineFamily at : EngineFamily.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
