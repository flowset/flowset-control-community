/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.test_support.engine.HasRunningEngineData;
import io.flowset.control.test_support.engine.external.ExternalRunningEngineExtension;
import io.flowset.control.test_support.testcontainers.EngineContainer;

/**
 * Cleans an engine container between tests in the case of shared instance of the container.
 *
 * @param <E> engine container type
 * @see RunningEngineExtension
 * @see WithRunningEngine#shared()
 * @see ExternalRunningEngineExtension
 */
public interface EngineDataCleaner<E extends HasRunningEngineData> {

    /**
     * Cleans the data in the specified engine container.
     *
     * @param engineContainer engine container
     */
    void clean(E engineContainer);

    /**
     * Checks whether data cleaning is supported for the specified engine container.
     *
     * @param engineContainer engine container
     * @return engine container is supported
     */
    boolean supports(HasRunningEngineData engineContainer);
}
