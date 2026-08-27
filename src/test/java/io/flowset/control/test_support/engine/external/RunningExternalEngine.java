/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.engine.external;

import io.flowset.control.test_support.property.ControlUiTestingProperties;

import java.lang.annotation.*;

/**
 * Annotates a field of type {@link ExternalEngine} value from {@link ControlUiTestingProperties#getEngine()}
 * into tests with {@link WithRunningExternalEngine} annotation.
 *
 * @see ExternalRunningEngineExtension
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RunningExternalEngine {
}
