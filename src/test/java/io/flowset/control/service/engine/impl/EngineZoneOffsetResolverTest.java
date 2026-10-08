/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.property.EngineConnectionCheckProperties;
import io.flowset.control.property.EngineProperties;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.auth.EngineAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EngineZoneOffsetResolverTest {

    private static final String DEPLOYMENTS_PATH = "/deployment?sortBy=deploymentTime&sortOrder=desc&maxResults=1";
    private static final String HISTORY_PATH = "/history/process-instance?sortBy=startTime&sortOrder=desc&maxResults=1";

    private final UUID engineId = UUID.randomUUID();
    private final Map<String, String> responses = new HashMap<>();
    private final AtomicInteger requestCount = new AtomicInteger();

    private TestEngineZoneOffsetResolver resolver;

    @BeforeEach
    void setUp() {
        BpmEngine engine = mock(BpmEngine.class);
        when(engine.getBaseUrl()).thenReturn("http://localhost:8080/engine-rest");

        EngineService engineService = mock(EngineService.class);
        when(engineService.findEngineByUuid(engineId)).thenReturn(engine);

        EngineProperties engineProperties = new EngineProperties(null, "Camunda", null, 10_000L, 3_600_000L);
        EngineConnectionCheckProperties checkProperties = new EngineConnectionCheckProperties(30, 1000, 1000, 1, 100);

        resolver = new TestEngineZoneOffsetResolver(engineService, mock(EngineAuthenticator.class),
                engineProperties, checkProperties);
    }

    @Test
    void givenDeployment_whenGetZoneOffset_thenOffsetOfDeploymentTimeReturned() {
        responses.put(DEPLOYMENTS_PATH, "[{\"id\":\"1\",\"deploymentTime\":\"2026-10-06T16:40:13.533+0300\"}]");

        assertThat(resolver.getZoneOffset(engineId)).contains(ZoneOffset.ofHours(3));
    }

    @Test
    void givenNoDeployments_whenGetZoneOffset_thenOffsetOfHistoricInstanceReturned() {
        responses.put(DEPLOYMENTS_PATH, "[]");
        responses.put(HISTORY_PATH, "[{\"id\":\"2\",\"startTime\":\"2026-10-06T16:40:13.533+1000\"}]");

        assertThat(resolver.getZoneOffset(engineId)).contains(ZoneOffset.ofHours(10));
    }

    @Test
    void givenNoData_whenGetZoneOffset_thenEmpty() {
        responses.put(DEPLOYMENTS_PATH, "[]");
        responses.put(HISTORY_PATH, "[]");

        assertThat(resolver.getZoneOffset(engineId)).isEmpty();
    }

    @Test
    void givenResolvedOffset_whenGetZoneOffsetAgain_thenCachedValueUsed() {
        responses.put(DEPLOYMENTS_PATH, "[{\"deploymentTime\":\"2026-10-06T16:40:13.533+0400\"}]");

        resolver.getZoneOffset(engineId);
        resolver.getZoneOffset(engineId);

        assertThat(requestCount.get()).isEqualTo(1);
    }

    @Test
    void givenCachedOffset_whenInvalidate_thenOffsetResolvedAgain() {
        responses.put(DEPLOYMENTS_PATH, "[{\"deploymentTime\":\"2026-10-06T16:40:13.533+0400\"}]");
        resolver.getZoneOffset(engineId);

        responses.put(DEPLOYMENTS_PATH, "[{\"deploymentTime\":\"2026-10-06T16:40:13.533+0200\"}]");
        resolver.invalidate(engineId);

        assertThat(resolver.getZoneOffset(engineId)).contains(ZoneOffset.ofHours(2));
        assertThat(requestCount.get()).isEqualTo(2);
    }

    @Test
    void givenUnknownEngine_whenGetZoneOffset_thenEmpty() {
        assertThat(resolver.getZoneOffset(UUID.randomUUID())).isEmpty();
        assertThat(requestCount.get()).isZero();
    }

    private class TestEngineZoneOffsetResolver extends EngineZoneOffsetResolver {

        TestEngineZoneOffsetResolver(EngineService engineService, EngineAuthenticator engineAuthenticator,
                                     EngineProperties engineProperties,
                                     EngineConnectionCheckProperties connectionCheckProperties) {
            super(engineService, engineAuthenticator, engineProperties, connectionCheckProperties);
        }

        @Override
        protected String requestEngine(BpmEngine engine, String path) {
            requestCount.incrementAndGet();
            return responses.get(path);
        }
    }
}
