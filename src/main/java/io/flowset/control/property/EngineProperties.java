/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@Setter
@ConfigurationProperties(prefix = "flowset.control.engine")
public class EngineProperties {

    /**
     * A base URL to BPM engine REST API, e.g. <code>http://localhost:8080/engine-rest</code>.
     */
    private String baseUrl;
    /**
     * A BPM engine name, e.g. Camunda.
     */
    private String name;

    /**
     * Authentication properties to connect to the BPM engine.
     */
    private AuthProperties auth;

    /**
     * Lifetime to store offsets in millis.
     */
    private Long offsetLifetimeInMillis;

    public EngineProperties(String baseUrl,
                            @DefaultValue("Camunda") String name,
                            AuthProperties auth,
                            @DefaultValue("10000") Long offsetLifetimeInMillis) {
        this.baseUrl = baseUrl;
        this.name = name;
        this.auth = auth;
        this.offsetLifetimeInMillis = offsetLifetimeInMillis;
    }

    @Getter
    @Setter
    public static class AuthProperties {
        /**
         * Properties to configure a basic authentication for BPM engine connection.
         */
        private BasicAuthProperties basic;
        /**
         * Properties to configure an authentication with the custom header for BPM engine connection.
         */
        private CustomHTTPHeaderAuthProperties customHttpHeader;
    }

    @Getter
    @Setter
    public static class BasicAuthProperties {
        /**
         * A username used for BPM engine connection in case of basic authentication.
         */
        private String username;
        /**
         * A password used for BPM engine connection in case of basic authentication.
         */
        private String password;
    }

    @Getter
    @Setter
    public static class CustomHTTPHeaderAuthProperties {
        /**
         * An HTTP header name used for BPM engine connection in case of API key authentication.
         */
        private String name;
        /**
         * An HTTP header value used for BPM engine connection in case of API key authentication.
         */
        private String value;
    }
}