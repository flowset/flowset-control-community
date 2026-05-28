/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

import io.jmix.core.security.EntityOp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define security constraints for operations on entities.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SecuredEntityOperation {

    /**
     * The entity class on which the operation is performed.
     * Should be used with {@link #entityOp()} field.
     *
     * @return entity class
     */
    Class<?> entityClass() default Void.class;

    /**
     * The type of operation to be performed on the entity.
     * Should be used with {@link #entityClass()} field.
     *
     * @return entity operation type
     */
    EntityOp entityOp() default EntityOp.READ;

    /**
     * Name of the permission to be checked.
     *
     * @return permission name
     */
    String specificPermission() default "";

    /**
     * Strategy to be applied when access to the secured resource is denied: returning null or throwing an exception.
     *
     * @return denied result strategy
     */
    DeniedResultStrategy deniedStrategy() default DeniedResultStrategy.THROW_ACCESS_DENIED;
}
