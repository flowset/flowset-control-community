/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.property.ControlEngineTestingProperties;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Enables executing a test on specific engine types.
 * <p>
 * Works when:
 * <ol>
 *     <li>The Spring profile <code>test-engine</code> is used</li>
 *     <li>The Spring profile <code>ui-test</code> is used and {@link ControlUiTestingProperties#getEngine()} type is one of available ones</li>
 * </ol>
 *
 * @see ControlEngineTestingProperties#getType()
 * @see ControlUiTestingProperties#getEngine()
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(EngineTypeCondition.class)
@Inherited
public @interface EnabledOnEngine {

    /**
     * A list of engine types for which the test should be run.
     *
     * @return engine types to which the test applies
     */
    EngineType[] value();
}
