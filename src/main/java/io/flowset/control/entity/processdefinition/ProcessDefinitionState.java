/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.processdefinition;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;


public enum ProcessDefinitionState implements EnumClass<String> {

    ACTIVE("Active"),
    SUSPENDED("Suspended");

    private final String id;

    ProcessDefinitionState(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ProcessDefinitionState fromId(String id) {
        for (ProcessDefinitionState at : ProcessDefinitionState.values()) {
            if (StringUtils.equalsIgnoreCase(at.getId(), id)) {
                return at;
            }
        }
        return null;
    }
}