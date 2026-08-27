/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.jspecify.annotations.Nullable;


public enum ProcessInstanceViewMode implements EnumClass<String> {

    ALL("all"),
    ACTIVE("active"),
    COMPLETED("completed");

    private final String id;

    ProcessInstanceViewMode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ProcessInstanceViewMode fromId(String id) {
        for (ProcessInstanceViewMode at : ProcessInstanceViewMode.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}