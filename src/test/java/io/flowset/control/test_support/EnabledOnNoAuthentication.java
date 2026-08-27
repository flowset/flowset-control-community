/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.test_support.engine.external.ExternalRunningEngineExtension;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Enables a test only when the BPM engine has no authentication configured.
 * For the {@code ui-test} profile, the test is skipped if:
 * <ol>
 *     <li>The configured external engine is missing</li>
 *     <li>Auth type in {@link ControlUiTestingProperties#getEngine()} is not {@code null}</li>
 * </ol>
 *
 * @see NoAuthenticationCondition
 * @see ExternalRunningEngineExtension
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Order(1)
@ExtendWith(NoAuthenticationCondition.class)
@Inherited
public @interface EnabledOnNoAuthentication {
}
