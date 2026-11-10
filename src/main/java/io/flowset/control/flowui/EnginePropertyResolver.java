/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.flowui;

import io.flowset.control.annotation.EngineProperty;
import io.flowset.control.entity.engine.EngineFamily;
import io.flowset.control.entity.engine.EngineType;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Evaluates {@link EngineProperty} of an entity attribute against an engine type. Both the engine types and the
 * families must allow it; a {@code null} engine type is checked as the Camunda 7 family.
 */
@Component("control_EnginePropertyResolver")
public class EnginePropertyResolver {

    /**
     * Checks whether the engine type supports the attribute according to its {@link EngineProperty}.
     */
    public boolean isSupported(MetaProperty metaProperty, @Nullable EngineType engineType) {
        EngineProperty engineProperty = metaProperty.getAnnotatedElement().getAnnotation(EngineProperty.class);
        if (engineProperty == null) {
            return true;
        }

        boolean supportedByType = isSupportedByType(engineType,
                engineProperty.supportedEngines(), engineProperty.unsupportedEngines());

        boolean supportedByFamily = isSupportedByFamily(getEngineFamily(engineType),
                engineProperty.supportedFamilies(), engineProperty.unsupportedFamilies());

        return supportedByType && supportedByFamily;
    }

    protected boolean isSupportedByType(@Nullable EngineType type, EngineType[] supportedTypes, EngineType[] unsupportedTypes) {
        if (supportedTypes.length > 0) {
            return ArrayUtils.contains(supportedTypes, type);
        }
        if (unsupportedTypes.length > 0) {
            return !ArrayUtils.contains(unsupportedTypes, type);
        }
        return true;
    }

    protected boolean isSupportedByFamily(EngineFamily family, EngineFamily[] supportedFamilies,
                                          EngineFamily[] unsupportedFamilies) {
        if (supportedFamilies.length > 0) {
            return ArrayUtils.contains(supportedFamilies, family);
        }
        if (unsupportedFamilies.length > 0) {
            return !ArrayUtils.contains(unsupportedFamilies, family);
        }
        return true;
    }

    private EngineFamily getEngineFamily(@Nullable EngineType engineType) {
        return engineType != null ? engineType.getFamily() : EngineFamily.CAMUNDA_7;
    }
}
