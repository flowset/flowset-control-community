/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.apache.commons.lang3.Strings;
import org.springframework.lang.Nullable;

public enum ExternalTaskState implements EnumClass<String> {

    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED");

    private final String id;

    ExternalTaskState(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ExternalTaskState fromId(String id) {
        for (ExternalTaskState state : ExternalTaskState.values()) {
            if (Strings.CI.equals(state.getId(), id)) {
                return state;
            }
        }
        return null;
    }
}
