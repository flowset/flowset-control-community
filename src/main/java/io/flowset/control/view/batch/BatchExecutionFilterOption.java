/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum BatchExecutionFilterOption implements EnumClass<String> {
    ALL("all"),
    WITH_FAILURES("withFailures"),
    WITHOUT_FAILURES("withoutFailures");

    private final String id;

    BatchExecutionFilterOption(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static BatchExecutionFilterOption fromId(String id) {
        for (BatchExecutionFilterOption state : BatchExecutionFilterOption.values()) {
            if (state.getId().equals(id)) {
                return state;
            }
        }
        return null;
    }
}
