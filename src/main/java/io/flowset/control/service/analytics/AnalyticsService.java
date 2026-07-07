package io.flowset.control.service.analytics;

import java.util.Map;

/**
 * Sends anonymous product usage analytics.
 * <p>
 * Events are tied to a random per-installation identifier (never a username), and collection can be
 * turned off by the administrator on the About page. Two implementations are provided and only one
 * is active depending on configuration:
 * <ul>
 *     <li>the Amplitude-backed implementation, active when an Amplitude key is configured;</li>
 *     <li>a no-op implementation, active when no key is set (e.g. the default open-source build),
 *     so nothing is sent and nothing is logged.</li>
 * </ul>
 * Implementations must be resilient: analytics must never break a user action, so failures
 * (network errors, missing context, disabled collection) are swallowed rather than propagated.
 */
public interface AnalyticsService {

    /**
     * Logs an analytics event of the given type with the default anonymous properties
     * (application version, UI language, product and license type).
     *
     * @param eventType the event type to log
     */
    void logEvent(AmplitudeEventType eventType);

    /**
     * Logs an analytics event with additional (anonymous, non-personal) event properties on top of
     * the default ones, e.g. project size metrics. The values are sent as event properties, so they
     * are retained over time in analytics.
     *
     * @param eventType       the event type to log
     * @param eventProperties additional non-personal properties to attach to the event;
     *                        must not contain personal data. May be empty
     */
    void logEvent(AmplitudeEventType eventType, Map<String, ?> eventProperties);
}
