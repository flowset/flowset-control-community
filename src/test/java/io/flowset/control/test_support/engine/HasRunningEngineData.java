/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.engine;

import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.EngineType;

/**
 * Provides information about the running BPM engine.
 */
public interface HasRunningEngineData {

    /**
     * @return a base URL of the REST API of the running engine
     */
    String getRestBaseUrl();

    /**
     * @return username for basic authentication
     */
    String getBasicAuthUsername();

    /**
     * @return password for basic authentication
     */
    String getBasicAuthPassword();

    /**
     * @return name of HTTP header using for authentication
     */
    String getAuthHeaderName();

    /**
     * @return value of HTTP header using for authentication
     */
    String getAuthHeaderValue();

    /**
     * @return type of the running engine
     */
    EngineType getEngineType();

    /**
     * @return type of authentication used for connecting to the running engine
     */
    AuthType getAuthType();

    /**
     * @return true if HTTP header authentication is enabled
     */
    default boolean isHeaderAuthEnabled() {
        return getAuthType() == AuthType.HTTP_HEADER;
    }

    /**
     * @return true if basic authentication is enabled
     */
    default boolean isBasicAuthEnabled() {
        return getAuthType() == AuthType.BASIC;
    }
}
