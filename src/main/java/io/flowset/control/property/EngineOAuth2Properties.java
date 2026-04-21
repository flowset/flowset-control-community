/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.property;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@ConfigurationProperties(prefix = "flowset.control.engine.oauth2")
@ConfigurationPropertiesBinding
public class EngineOAuth2Properties {

    /**
     * Maximum number of failed attempts for getting an access token before lock.
     */
    @Positive
    protected int maxRetries;

    /**
     * Whether to lock the engine if the maximum number of failed attempts is reached.
     */
    protected boolean lockEnabled;

    public EngineOAuth2Properties(@DefaultValue("5") int maxRetries,
                                  @DefaultValue("false") boolean lockEnabled) {
        this.maxRetries = maxRetries;
        this.lockEnabled = lockEnabled;
    }
}
