/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.alltasks;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;


public enum UserTaskStateFilterOption implements EnumClass<String> {

    ALL("All"),
    ACTIVE("Active"),
    SUSPENDED("Suspended");

    private final String id;

    UserTaskStateFilterOption(String id) {
        this.id = id;

    }

    public String getId() {
        return id;
    }

    @Nullable
    public static UserTaskStateFilterOption fromId(String id) {
        for (UserTaskStateFilterOption at : UserTaskStateFilterOption.values()) {
            if (Strings.CI.equals(at.getId(), id)) {
                return at;
            }
        }
        return null;
    }
}