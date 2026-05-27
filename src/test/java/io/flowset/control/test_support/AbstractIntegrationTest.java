/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.jmix.core.session.SessionData;
import io.flowset.control.FlowsetControlApplication;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.flowset.control.entity.engine.BpmEngine.SELECTED_ENGINE_ATTRIBUTE;

/**
 * A base class for integration test.
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {FlowsetControlApplication.class, FlowsetControlTestConfiguration.class})
@Tag("integrationTest")
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3")
            .withDatabaseName("flowset-control-test")
            .withUsername("root")
            .withPassword("root");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ObjectProvider<SessionData> sessionDataProvider;

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("main.datasource.url", postgres::getJdbcUrl);
        registry.add("main.datasource.username", postgres::getUsername);
        registry.add("main.datasource.password", postgres::getPassword);
        registry.add("main.datasource.driver-class-name", postgres::getDriverClassName);
    }

    protected void resetSelectedEngine() {
        sessionDataProvider.getObject().setAttribute(SELECTED_ENGINE_ATTRIBUTE, null);

        jdbcTemplate.execute("DELETE FROM control_bpm_engine");
    }


}
