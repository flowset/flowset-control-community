/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.batch;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum BatchState implements EnumClass<String> {
    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED"),
    COMPLETED("COMPLETED");

    private final String id;

    BatchState(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static BatchState fromId(String id) {
        for (BatchState state : BatchState.values()) {
            if (state.getId().equals(id)) {
                return state;
            }
        }
        return null;
    }
}
