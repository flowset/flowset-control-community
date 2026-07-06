/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@Setter
@ConfigurationProperties(prefix = "flowset.control.analytics")
public class AnalyticsProperties {

    /**
     * Default state of anonymous analytics collection, applied once when the
     * {@code AnalyticsSettings} row is first created. Afterwards the state is
     * controlled by the administrator via the About page and stored in the database.
     */
    private final boolean enabled;

    public AnalyticsProperties(@DefaultValue("true") boolean enabled) {
        this.enabled = enabled;
    }
}
