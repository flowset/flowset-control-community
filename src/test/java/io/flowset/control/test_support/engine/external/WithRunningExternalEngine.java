/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.engine.external;

import io.flowset.control.test_support.property.ControlUiTestingProperties;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Marks a UI test class that runs against an externally deployed BPM engine stand.
 *
 * <p>Engine name and environment type are taken from {@link ExternalEngine} (configured via
 * {@link ControlUiTestingProperties#getEngine()} properties).
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({ExternalRunningEngineExtension.class})
@Inherited
public @interface WithRunningExternalEngine {

    /**
     * Whether a BPM engine row should be inserted in the Control DB before each test
     * (and removed after). Defaults to {@code true} since most UI tests assume an engine is registered.
     */
    boolean save() default true;
}
