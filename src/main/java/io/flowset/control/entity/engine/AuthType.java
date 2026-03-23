/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.engine;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;


public enum AuthType implements EnumClass<String> {

    BASIC("Basic"),
    HTTP_HEADER("HTTP header"),
    OAUTH2("OAuth2");

    private final String id;

    AuthType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AuthType fromId(String id) {
        for (AuthType at : AuthType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
