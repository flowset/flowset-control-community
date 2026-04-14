/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.batch;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum BatchStateFilterOption implements EnumClass<String> {
    ALL("ALL"),
    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED");

    protected final String id;

    BatchStateFilterOption(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static BatchStateFilterOption fromId(String id) {
        for (BatchStateFilterOption state : BatchStateFilterOption.values()) {
            if (state.getId().equals(id)) {
                return state;
            }
        }
        return null;
    }
}
