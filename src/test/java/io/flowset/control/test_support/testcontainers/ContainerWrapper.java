/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.testcontainers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for engine container to store as closable resource in {@link ExtensionContext.Store}.
 *
 * @param <V> - engine container type
 */
public class ContainerWrapper<V extends EngineContainer<?>> implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ContainerWrapper.class);

    private final V container;

    public ContainerWrapper(V container) {
        this.container = container;
    }

    public ContainerWrapper<V> start() {
        container.start();
        return this;
    }

    @Override
    public void close() {
        String containerId = container.getContainerId();

        log.info("Stopping engine container {} {}", container.getDockerImageName(), containerId);
        container.stop();
        log.info("Stopped engine container {} {}", container.getDockerImageName(), containerId);
    }

    public V getContainer() {
        return container;
    }
}
