/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.property;

import io.flowset.control.test_support.EnabledOnBasicAuthentication;
import io.flowset.control.test_support.EnabledOnEngine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Profile;

import static io.flowset.control.test_support.Constants.TEST_ENGINE_PROFILE;

/**
 * Properties to run tests for specific engine type, docker image and engine authentication.
 */
@Profile(TEST_ENGINE_PROFILE)
@ConfigurationProperties(prefix = "flowset.control.testing.engine")
public class ControlEngineTestingProperties {
    /**
     * A type of BPM engine to be tested.
     *
     * @see EnabledOnEngine
     */
    private String type;
    /**
     * A docker image name (with tag) of the BPM engine that should be used to run the BPM engine test container.
     * Supported docker images: Camunda Run (e.g. camunda/camunda-bpm-platform:run-7.21.0), Operaton (operaton/operaton-1.0.0-beta3)
     */
    private String dockerImage;

    /**
     * A type of authentication that is used for connecting to the BPM engine test container.
     * Supported values: empty or null string, Basic
     *
     * @see io.flowset.control.entity.engine.AuthType
     * @see EnabledOnBasicAuthentication
     */
    private String authType;

    public ControlEngineTestingProperties(@DefaultValue("camunda_7") String type,
                                          @DefaultValue("camunda/camunda-bpm-platform:run-7.22.0") String dockerImage,
                                          String authType) {
        this.type = type;
        this.dockerImage = dockerImage;
        this.authType = authType;
    }

    /**
     * Sets a type of BPM engine to be tested.
     *
     * @param type type of BPM engine to be tested
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets a docker image name (with tag) of the BPM engine that should be used to run the BPM engine test container
     *
     * @param dockerImage supported docker image, e.g. camunda/camunda-bpm-platform:run-7.21.0
     */
    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    /**
     * Sets a type of authentication that is used for connecting to the BPM engine test container
     *
     * @param authType supported authentication type
     */
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * @return type of BPM engine to be tested
     */
    public String getType() {
        return type;
    }

    /**
     * @return docker image name of the BPM engine that should be used to run the BPM engine test container
     */
    public String getDockerImage() {
        return dockerImage;
    }

    /**
     * @return type of authentication that is used for connecting to the BPM engine test container
     */
    public String getAuthType() {
        return authType;
    }
}
