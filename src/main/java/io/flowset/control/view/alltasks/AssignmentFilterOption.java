/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.alltasks;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;


public enum AssignmentFilterOption implements EnumClass<String> {

    ASSIGNED("ASSIGNED"),
    UNASSIGNED("UNASSIGNED");

    private final String id;

    AssignmentFilterOption(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AssignmentFilterOption fromId(String id) {
        for (AssignmentFilterOption at : AssignmentFilterOption.values()) {
            if (Strings.CI.equals(at.getId(), id)) {
                return at;
            }
        }
        return null;
    }
}