/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.engine.external;

import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.camunda7.CamundaDataCleaner;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.engine.HasRunningEngineData;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an external engine for testing purposes.
 * Contains information about the engine's like base URL, type, authentication type, etc.
 * This detail is used to create a default {@link BpmEngine} instance for the tests that
 * annotated with {@link @WithRunningExternalEngine}.
 *
 * @see ExternalRunningEngineExtension
 * @see WithRunningExternalEngine
 * @see CamundaRestTestHelper
 * @see CamundaDataCleaner
 */
@Getter
@Setter
public class ExternalEngine implements HasRunningEngineData {
    private String name;

    @NotBlank
    private String restBaseUrl;

    private EngineType type = EngineType.CAMUNDA_7;

    private AuthType authType;

    private String basicAuthUsername;
    private String basicAuthPassword;

    private String authHeaderName;
    private String authHeaderValue;

    @Override
    public EngineType getEngineType() {
        return type;
    }
}
