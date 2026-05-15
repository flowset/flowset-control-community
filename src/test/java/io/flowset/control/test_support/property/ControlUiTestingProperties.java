/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.property;

import io.flowset.control.test_support.engine.external.ExternalEngine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

import static io.flowset.control.test_support.Constants.UI_TEST_PROFILE;

/**
 * Properties consumed by UI tests when they run against external Control + Camunda 7 stands.
 */
@Getter
@ConfigurationProperties(prefix = "flowset.control.testing.ui")
@Profile(UI_TEST_PROFILE)
public class ControlUiTestingProperties {

    /**
     * Base URL of the Control instance (used by Selenide).
     */
    @NotBlank
    private final String controlUrl;

    /**
     * Configured external BPM engine data, e.g., Camunda 7
     */
    private final ExternalEngine engine;

    /**
     * Admin username for logging into the deployed Control via the UI.
     */
    private final String adminUsername;

    /**
     * Admin password for logging into the deployed Control via the UI.
     */
    private final String adminPassword;

    public ControlUiTestingProperties(@NotBlank String controlUrl,
                                      @Valid ExternalEngine engine,
                                      String adminUsername,
                                      String adminPassword) {
        this.controlUrl = controlUrl;
        this.engine = engine;
        this.adminUsername = adminUsername == null || adminUsername.isBlank() ? "admin" : adminUsername;
        this.adminPassword = adminPassword == null || adminPassword.isBlank() ? "admin" : adminPassword;
    }
}
