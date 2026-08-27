/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.testcontainers;

import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.engine.HasRunningEngineData;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * A base class for the BPM engine test container.
 *
 * @param <SELF>
 */
public abstract class EngineContainer<SELF extends EngineContainer<SELF>> extends GenericContainer<SELF>
        implements HasRunningEngineData {

    protected AuthType authType;

    protected String basicAuthUsername;
    protected String basicAuthPassword;
    protected String authHeaderName;
    protected String authHeaderValue;
    protected EngineType engineType;


    public EngineContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    /**
     * @return base URL of the BPM engine REST API
     */
    public abstract String getRestBaseUrl();

    /**
     * @return version of the BPM engine running using this container
     */
    public abstract String getVersion();

    /**
     * @return type of the BPM engine running using this container
     */
    public EngineType getEngineType() {
        return engineType;
    }

    /**
     * @return basic authentication is configured to connect using REST API
     */
    public boolean isBasicAuthEnabled() {
        return authType == AuthType.BASIC;
    }

    /**
     * @return authentication using HTTP is configured to connect using REST API
     */
    public boolean isHeaderAuthEnabled() {
        return authType == AuthType.HTTP_HEADER;
    }

    /**
     * @return password for basic authentication
     */
    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    /**
     * @return username for basic authentication
     */
    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    /**
     * @return engine authentication type
     */
    public AuthType getAuthType() {
        return authType;
    }

    /**
     * @return name of HTTP header using for authentication
     */
    public String getAuthHeaderName() {
        return authHeaderName;
    }

    /**
     * @return value of HTTP header using for authentication
     */
    public String getAuthHeaderValue() {
        return authHeaderValue;
    }

    /**
     * Sets an authentication type for the BPM engine.
     *
     * @param authType authentication type
     * @return current instance of container
     */
    public SELF withAuthType(final AuthType authType) {
        this.authType = authType;
        return self();
    }

    /**
     * Sets a username for basic authentication.
     *
     * @param basicAuthUsername username
     * @return current instance of container
     */
    public SELF withBasicAuthUsername(final String basicAuthUsername) {
        this.basicAuthUsername = basicAuthUsername;
        return self();
    }

    /**
     * Sets a password for basic authentication.
     *
     * @param basicAuthPassword password
     * @return current instance of container
     */
    public SELF withBasicAuthPassword(final String basicAuthPassword) {
        this.basicAuthPassword = basicAuthPassword;
        return self();
    }
}
