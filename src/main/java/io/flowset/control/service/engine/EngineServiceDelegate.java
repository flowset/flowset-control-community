/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine;

import io.flowset.control.security.SecuredOperationsAspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service implementation that {@link EngineOperationServiceProvider} selects for an engine type and that is
 * called only through the facade of its service. {@link SecuredOperationsAspect} does not check the methods of a
 * marked class: the facade has checked the call already.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EngineServiceDelegate {
}
