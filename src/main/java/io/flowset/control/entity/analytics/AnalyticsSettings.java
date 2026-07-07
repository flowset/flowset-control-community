/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.analytics;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Instance-wide settings for anonymous product analytics.
 * <p>
 * A single row is expected: it stores the randomly generated anonymous
 * {@code installationId} used as the analytics user id (no PII), and the
 * {@code enabled} flag toggled by the administrator on the About page.
 */
@JmixEntity
@Table(name = "CONTROL_ANALYTICS_SETTINGS")
@Entity
@SystemLevel
public class AnalyticsSettings {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "INSTALLATION_ID", nullable = false)
    @NotNull
    private UUID installationId;

    @Column(name = "ENABLED", nullable = false)
    @NotNull
    private Boolean enabled = true;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInstallationId() {
        return installationId;
    }

    public void setInstallationId(UUID installationId) {
        this.installationId = installationId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
