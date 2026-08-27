/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies security settings for loading secured resources.
 * This annotation is applied to methods to determine the behavior when access
 * to a resource is denied.
 * <br/>
 * The `entityClass` element specifies the class of the entity to be secured,
 * while the `deniedStrategy` element defines the action to be taken when access is denied.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SecuredEntityLoad {

    /**
     * Specifies the class of the entity what load to be secured.
     *
     * @return the class of the secured entity
     */
    Class<?> entityClass();

    /**

     * Specifies the strategy to be applied when access to the secured resource is denied: returning null, throwing an exception,
     * or returning a default value.
     * Defaults to {@link DeniedResultStrategy#TYPE_DEFAULT}.
     *
     * @return the denied result strategy
     */
    DeniedResultStrategy deniedStrategy() default DeniedResultStrategy.TYPE_DEFAULT;
}
