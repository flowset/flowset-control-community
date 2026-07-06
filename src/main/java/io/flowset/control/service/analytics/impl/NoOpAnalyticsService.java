/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.analytics.impl;

import io.flowset.control.service.analytics.AmplitudeEventType;
import io.flowset.control.service.analytics.AnalyticsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * No-op analytics implementation, active when no Amplitude key is configured
 * (e.g. the open-source build without a build-time injected key).
 * <p>
 * Keeping a dedicated no-op bean avoids per-event branching and log spam:
 * with no key configured nothing is sent and nothing is logged.
 */
@Service("control_AnalyticsService")
@ConditionalOnExpression("'${amplitude.key:}'.trim().isEmpty()")
public class NoOpAnalyticsService implements AnalyticsService {

    @Override
    public void logEvent(AmplitudeEventType eventType) {
        // intentionally does nothing
    }

    @Override
    public void logEvent(AmplitudeEventType eventType, Map<String, ?> eventProperties) {
        // intentionally does nothing
    }
}
