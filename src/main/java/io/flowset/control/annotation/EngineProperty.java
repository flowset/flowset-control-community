/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.annotation;

import io.flowset.control.entity.engine.EngineFamily;
import io.flowset.control.entity.engine.EngineType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity attribute that is supported only by some engine types. Set only one of the attributes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EngineProperty {

    /**
     * The attribute is supported only when the selected engine type is one of these.
     */
    EngineType[] supportedEngines() default {};

    /**
     * The attribute is not supported when the selected engine type is one of these.
     */
    EngineType[] unsupportedEngines() default {};

    /**
     * The attribute is supported only when the selected engine type belongs to one of these families.
     */
    EngineFamily[] supportedFamilies() default {};

    /**
     * The attribute is not supported when the selected engine type belongs to one of these families.
     */
    EngineFamily[] unsupportedFamilies() default {};
}
