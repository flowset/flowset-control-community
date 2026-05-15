/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.test_support.engine.external.ExternalRunningEngineExtension;
import io.flowset.control.test_support.property.ControlEngineTestingProperties;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Enables to run the test if BPM engine running in the test container has configured basic authentication.
 * The test is skipped if:
 * <ol>
 *     <li>The Spring profile <code>test-engine</code> is used and {@link ControlEngineTestingProperties#getAuthType()} is not Basic</li>
 *     <li>The Spring profile <code>ui-test</code> is used and auth type in {@link ControlUiTestingProperties#getEngine()} is not Basic</li>
 * </ol>
 *
 * @see BasicAuthenticationCondition
 * @see RunningEngineExtension
 * @see ExternalRunningEngineExtension
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Order(1)
@ExtendWith(BasicAuthenticationCondition.class)
@Inherited
public @interface EnabledOnBasicAuthentication {
}
