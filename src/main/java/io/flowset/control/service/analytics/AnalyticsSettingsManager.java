/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.analytics;

import io.flowset.control.entity.analytics.AnalyticsSettings;
import io.flowset.control.property.AnalyticsProperties;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides access to instance-wide {@link AnalyticsSettings}.
 * <p>
 * Ensures a single settings row exists (creating it with a random anonymous
 * {@code installationId} on first run) and caches the current values to avoid a
 * database round-trip on every tracked event.
 */
@Slf4j
@Component("control_AnalyticsSettingsManager")
public class AnalyticsSettingsManager {

    protected final DataManager dataManager;
    protected final SystemAuthenticator systemAuthenticator;
    protected final AnalyticsProperties analyticsProperties;

    protected volatile UUID installationId;
    protected volatile boolean enabled;

    public AnalyticsSettingsManager(DataManager dataManager,
                                    SystemAuthenticator systemAuthenticator,
                                    AnalyticsProperties analyticsProperties) {
        this.dataManager = dataManager;
        this.systemAuthenticator = systemAuthenticator;
        this.analyticsProperties = analyticsProperties;
    }

    @EventListener
    public void onApplicationStarted(ApplicationStartedEvent event) {
        try {
            AnalyticsSettings settings = getOrCreate();
            this.installationId = settings.getInstallationId();
            this.enabled = Boolean.TRUE.equals(settings.getEnabled());
        } catch (Exception e) {
            log.warn("Unable to initialize analytics settings", e);
        }
    }

    /**
     * @return the anonymous installation id used as the analytics user id.
     */
    public UUID getInstallationId() {
        if (installationId == null) {
            AnalyticsSettings settings = getOrCreate();
            this.installationId = settings.getInstallationId();
            this.enabled = Boolean.TRUE.equals(settings.getEnabled());
        }
        return installationId;
    }

    /**
     * @return whether anonymous analytics collection is currently enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Persists the new enabled state and refreshes the cache.
     */
    public void setEnabled(boolean value) {
        systemAuthenticator.withSystem(() -> {
            AnalyticsSettings settings = loadOrCreate();
            settings.setEnabled(value);
            dataManager.save(settings);
            return null;
        });
        this.enabled = value;
    }

    protected AnalyticsSettings getOrCreate() {
        return systemAuthenticator.withSystem(this::loadOrCreate);
    }

    /**
     * Loads the single settings row or creates it. Must be called within a system
     * authentication context (see {@link #getOrCreate()} / {@link #setEnabled(boolean)}).
     */
    protected AnalyticsSettings loadOrCreate() {
        Optional<AnalyticsSettings> existing = dataManager.load(AnalyticsSettings.class)
                .all()
                .maxResults(1)
                .optional();
        if (existing.isPresent()) {
            return existing.get();
        }
        AnalyticsSettings settings = dataManager.create(AnalyticsSettings.class);
        settings.setInstallationId(UUID.randomUUID());
        settings.setEnabled(analyticsProperties.isEnabled());
        return dataManager.save(settings);
    }
}
