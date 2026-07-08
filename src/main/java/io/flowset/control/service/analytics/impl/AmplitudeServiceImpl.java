package io.flowset.control.service.analytics.impl;

import com.amplitude.Amplitude;
import com.amplitude.Event;
import com.vaadin.flow.server.VaadinRequest;
import io.flowset.control.service.analytics.AmplitudeEventType;
import io.flowset.control.service.analytics.AnalyticsService;
import io.flowset.control.service.analytics.AnalyticsSettingsManager;
import io.jmix.core.security.CurrentAuthentication;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service("control_AnalyticsService")
public class AmplitudeServiceImpl implements AnalyticsService {
    public static final String PRODUCT = "flowset";
    public static final String LICENSE_TYPE = "community";

    // Build-info property that carries the Amplitude API key, injected at build time (see build.gradle).
    protected static final String ANALYTICS_KEY = "analyticsKey";
    protected static final String BUILD_TYPE = "buildType";

    protected final CurrentAuthentication currentAuthentication;
    protected final AnalyticsSettingsManager analyticsSettingsManager;
    protected final BuildProperties buildProperties;

    // Null when no key is configured (e.g. the default open-source build): analytics is then a no-op.
    protected Amplitude client;

    public AmplitudeServiceImpl(AnalyticsSettingsManager analyticsSettingsManager,
                                BuildProperties buildProperties,
                                CurrentAuthentication currentAuthentication) {
        this.analyticsSettingsManager = analyticsSettingsManager;
        this.currentAuthentication = currentAuthentication;
        this.buildProperties = buildProperties;

        String apiKey = buildProperties.get(ANALYTICS_KEY);
        if (apiKey != null && !apiKey.isBlank()) {
            client = Amplitude.getInstance();
            client.init(apiKey);
        }
    }

    @Override
    public void logEvent(AmplitudeEventType eventType) {
        logEvent(eventType, java.util.Collections.emptyMap());
    }

    @Override
    public void logEvent(AmplitudeEventType eventType, Map<String, ?> eventProperties) {
        // No key configured -> nothing is sent (and nothing is logged).
        if (client == null) {
            return;
        }
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
            properties.put("app_build_type", buildProperties.get(BUILD_TYPE));
            properties.put("app_language", resolveLanguage());
            properties.put("license_type", LICENSE_TYPE);
            properties.put("product", PRODUCT);

            // Additional non-personal properties (e.g. dashboard size metrics).
            if (eventProperties != null) {
                eventProperties.forEach(properties::put);
            }

            event.eventProperties = properties;

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
        if (client == null) {
            return;
        }
        try {
            client.flushEvents();
            client.shutdown();
        } catch (Exception e) {
            log.debug("Error while shutting down the Amplitude client", e);
        }
    }
}
