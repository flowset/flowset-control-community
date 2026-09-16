/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
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
 * A test container for CIB seven (Run distribution) docker images, e.g. {@code cibseven/cibseven:run-2.2.0}.
 */
public class CibSevenContainer extends Camunda7Container<CibSevenContainer> {
    public static final String IMAGE_NAME = "cibseven/cibseven";

    public static final Integer SERVER_PORT = 8080;

    public static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(IMAGE_NAME);

    private final Map<String, String> engineVersionsByTag = Map.of();

    public CibSevenContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public CibSevenContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        // CIB seven Run keeps the Camunda 7 Run main class name (org.cibseven.bpm.run.CamundaBpmRun)
        this.waitStrategy = Wait
                .forLogMessage(".*Started CamundaBpmRun.*", 1)
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
        if (versionPart.equals("latest") || versionPart.endsWith("-latest")) {
            return "";
        }
        if (versionPart.startsWith("run-")) {
            return versionPart.substring("run-".length());
        }
        if (versionPart.startsWith("run4-")) {
            return versionPart.substring("run4-".length());
        }
        return engineVersionsByTag.get(versionPart);
    }

    @Override
    public EngineType getEngineType() {
        return EngineType.CIB_SEVEN;
    }

    @Override
    protected void configure() {
        // CIB seven renamed the Spring Boot property prefix 'camunda.bpm' to 'cibseven.bpm',
        // so the Run distribution environment variables use the CIBSEVEN_ prefix
        addEnv("CIBSEVEN_BPM_RUN_AUTH_ENABLED", authType == AuthType.BASIC ? "true" : "false");
        addEnv("CIBSEVEN_BPM_ADMIN_USER_ID", basicAuthPassword);
        addEnv("CIBSEVEN_BPM_ADMIN_USER_PASSWORD", basicAuthPassword);
        addEnv("SERVER_PORT", SERVER_PORT.toString());

        setCommand("./cibseven.sh", "--rest");
    }
}
