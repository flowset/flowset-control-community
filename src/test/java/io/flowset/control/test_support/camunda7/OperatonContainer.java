/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.EngineType;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * A test container for Operaton (Spring Boot version) docker images.
 */
public class OperatonContainer extends Camunda7Container<OperatonContainer> {
    public static final String IMAGE_NAME = "operaton/operaton";

    public static final Integer SERVER_PORT = 8080;

    public static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(IMAGE_NAME);

    private final Map<String, String> engineVersionsByTag = Map.of(
            "1.0.0-beta-3", "1.0.0-beta-3-SNAPSHOT"
    );

    public OperatonContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public OperatonContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        this.waitStrategy = Wait
                .forLogMessage(".*Started OperatonApp.*", 1)
                .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));

        addExposedPort(SERVER_PORT);
    }

    @Override
    public String getRestBaseUrl() {
        return "http://" + getHost() + ":" + getMappedPort(SERVER_PORT) + "/engine-rest";
    }

    @Override
    public String getVersion() {
        DockerImageName dockerImageName = DockerImageName.parse(getDockerImageName());
        String versionPart = dockerImageName.getVersionPart();
        if (versionPart.equals("latest")) {
            return "";
        }
        if (versionPart.startsWith("run-")) {
            return versionPart.substring("run-".length());
        }
        return engineVersionsByTag.get(versionPart);
    }

    @Override
    public EngineType getEngineType() {
        return EngineType.OPERATON;
    }

    @Override
    protected void configure() {
        addEnv("OPERATON_BPM_RUN_AUTH_ENABLED", authType == AuthType.BASIC ? "true" : "false");
        addEnv("OPERATON_BPM_ADMIN_USER_ID", basicAuthPassword);
        addEnv("OPERATON_BPM_ADMIN_USER_PASSWORD", basicAuthPassword);
        addEnv("SERVER_PORT", SERVER_PORT.toString());

        setCommand("./operaton.sh", "--rest");
    }
}
