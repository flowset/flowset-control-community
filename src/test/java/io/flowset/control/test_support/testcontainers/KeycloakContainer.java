/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * A test container for Keycloak docker images. The container is configured to import a test realm.
 * @param <SELF>
 */
public class KeycloakContainer<SELF extends KeycloakContainer<SELF>> extends GenericContainer<SELF> {
    public static final String IMAGE_NAME = "quay.io/keycloak/keycloak:26.5.4";
    public static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(IMAGE_NAME);
    public static final Integer SERVER_PORT = 8080;

    public KeycloakContainer() {
        this(DEFAULT_IMAGE_NAME);
    }

    public KeycloakContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public KeycloakContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        this.waitStrategy = Wait.forHttp("/realms/master/.well-known/openid-configuration")
                .forPort(SERVER_PORT)
                .forStatusCode(200);

        addExposedPort(SERVER_PORT);
    }

    @Override
    protected void configure() {
        addEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin");
        addEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin");


        withCopyFileToContainer(MountableFile.forClasspathResource("test_support/keycloak/control-test-realm.json"),
                "/opt/keycloak/data/import/control-test-realm.json");

        setCommand("start-dev --import-realm --http-port=%s".formatted(SERVER_PORT));
    }

    public String getIssuerUri() {
        return "http://" + getHost() + ":" + getMappedPort(SERVER_PORT) + "/realms/control-test";
    }

}
