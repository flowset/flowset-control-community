/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

/**
 * Where a resource referenced from a BPMN model is found.
 */
public enum ReferenceLocation implements EnumClass<String> {

    /**
     * The resource is uploaded to the same deployment.
     */
    THIS_DEPLOYMENT("thisDeployment"),
    /**
     * The resource is already deployed to the engine.
     */
    ENGINE("engine"),
    /**
     * The resource is found neither in the deployment nor in the engine.
     */
    MISSING("missing"),
    /**
     * The reference cannot be checked: a dynamic key, a resource that cannot be looked up in the engine
     * or the engine is unavailable.
     */
    NOT_CHECKED("notChecked"),
    /**
     * The reference is being checked in the engine (the engine lookup is performed asynchronously).
     */
    CHECKING("checking");

    private final String id;

    ReferenceLocation(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ReferenceLocation fromId(@Nullable String id) {
        for (ReferenceLocation value : ReferenceLocation.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
