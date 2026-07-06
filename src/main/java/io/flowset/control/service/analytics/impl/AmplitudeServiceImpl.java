package io.flowset.control.service.analytics.impl;

import com.amplitude.Amplitude;
import com.amplitude.Event;
import com.vaadin.flow.server.VaadinRequest;
import io.flowset.control.service.analytics.AmplitudeEventType;
import io.flowset.control.service.analytics.AmplitudeProperties;
import io.flowset.control.service.analytics.AnalyticsService;
import io.flowset.control.service.analytics.AnalyticsSettingsManager;
import io.jmix.core.security.CurrentAuthentication;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service("control_AnalyticsService")
@ConditionalOnExpression("!'${amplitude.key:}'.trim().isEmpty()")
public class AmplitudeServiceImpl implements AnalyticsService {
    public static final String PRODUCT = "flowset";
    public static final String LICENSE_TYPE = "community";

    protected final CurrentAuthentication currentAuthentication;
    protected final AmplitudeProperties amplitudeProperties;
    protected final AnalyticsSettingsManager analyticsSettingsManager;
    protected final BuildProperties buildProperties;

    protected Amplitude client;

    public AmplitudeServiceImpl(AmplitudeProperties amplitudeProperties,
                                AnalyticsSettingsManager analyticsSettingsManager,
                                BuildProperties buildProperties,
                                CurrentAuthentication currentAuthentication) {
        this.amplitudeProperties = amplitudeProperties;
        this.analyticsSettingsManager = analyticsSettingsManager;
        this.currentAuthentication = currentAuthentication;
        this.buildProperties = buildProperties;

        client = Amplitude.getInstance();
        client.init(amplitudeProperties.getKey());
    }

    @Override
    public void logEvent(AmplitudeEventType eventType) {
        logEvent(eventType, java.util.Collections.emptyMap());
    }

    @Override
    public void logEvent(AmplitudeEventType eventType, Map<String, ?> eventProperties) {
        // Respect the runtime toggle (About page); disabled by the administrator -> nothing is sent.
        if (!analyticsSettingsManager.isEnabled()) {
            return;
        }

        // Analytics must never break a user action, so any failure here is swallowed and logged.
        try {
            // Anonymous: the analytics user id is a random per-installation UUID, not the username.
            String installationId = analyticsSettingsManager.getInstallationId().toString();

            Event event = new Event(eventType.getId(), installationId);

            // Forward the client IP (from the X-Forwarded-For header) when available.
            // getCurrent() may be null on non-request threads, so it is guarded.
            String ip = resolveClientIp();
            if (ip != null) {
                event.ip = ip;
            }

            JSONObject properties = new JSONObject();
            // Use put (not append): append wraps values into JSON arrays, breaking Amplitude property filtering.
            properties.put("app_version", buildProperties.getVersion());
            properties.put("app_language", resolveLanguage());
            properties.put("license_type", LICENSE_TYPE);
            properties.put("product", PRODUCT);

            // Additional non-personal properties (e.g. dashboard size metrics).
            if (eventProperties != null) {
                eventProperties.forEach(properties::put);
            }

            event.eventProperties = properties;

            // This bean is only active when a non-empty key is configured (see @ConditionalOnExpression),
            // so the event is always sent; the no-key case is handled by NoOpAnalyticsService.
            client.logEvent(event);
        } catch (Exception e) {
            log.debug("Failed to log analytics event '{}'", eventType.getId(), e);
        }
    }

    /**
     * @return the current authentication language, or {@code null} if there is no
     * authenticated context (e.g. background thread). Never throws.
     */
    protected String resolveLanguage() {
        if (currentAuthentication.isSet()) {
            return currentAuthentication.getLocale().getLanguage();
        }
        return null;
    }

    /**
     * @return the client IP from the {@code X-Forwarded-For} header, or {@code null} if there is
     * no current request (e.g. background thread). Never throws.
     */
    protected String resolveClientIp() {
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request != null) {
            return request.getHeader("X-Forwarded-For");
        }
        return null;
    }

    /**
     * Flush buffered events and release the SDK resources on shutdown so that
     * queued events are not lost.
     */
    @PreDestroy
    public void shutdown() {
        try {
            client.flushEvents();
            client.shutdown();
        } catch (Exception e) {
            log.debug("Error while shutting down the Amplitude client", e);
        }
    }
}
