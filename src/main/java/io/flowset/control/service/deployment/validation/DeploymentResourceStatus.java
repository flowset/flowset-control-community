/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

/**
 * Validation status of a deployment resource.
 */
public enum DeploymentResourceStatus implements EnumClass<String> {

    OK("ok"),
    WARNING("warning"),
    ERROR("error");

    private final String id;

    DeploymentResourceStatus(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static DeploymentResourceStatus fromId(@Nullable String id) {
        for (DeploymentResourceStatus status : DeploymentResourceStatus.values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }
}
