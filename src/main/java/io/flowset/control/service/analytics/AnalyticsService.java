package io.flowset.control.service.analytics;

import java.util.Map;

public interface AnalyticsService {

    void logEvent(AmplitudeEventType eventType);

    /**
     * Logs an event with additional (anonymous, non-personal) event properties, e.g. dashboard
     * size metrics. Sent as event properties so the values are kept over time in analytics.
     *
     * @param eventType       the event type
     * @param eventProperties additional non-personal properties to attach to the event
     */
    void logEvent(AmplitudeEventType eventType, Map<String, ?> eventProperties);
}
