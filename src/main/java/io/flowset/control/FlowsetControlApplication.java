/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control;

import com.google.common.base.Strings;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import io.jmix.flowui.theme.lumo.JmixLumo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Push
@PWA(name = "Flowset Control", shortName = "Flowset Control", iconPath = "icons/logo.png", offline = false)
@SpringBootApplication
@ConfigurationPropertiesScan
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet(JmixLumo.STYLESHEET)
@StyleSheet("themes/flowset/styles.css") // flowset-theme dependency
@StyleSheet("themes/flowset-control/styles.css") // project sources
public class FlowsetControlApplication
        extends SpringBootServletInitializer
        implements AppShellConfigurator {

    @Autowired
    private Environment environment;

    /**
     * Entry point for WAR deployment (WildFly / external servlet container).
     * Standard {@link #main} entry point remains fully functional for JAR/Docker.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(FlowsetControlApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(FlowsetControlApplication.class, args);
    }

    @Bean
    @Primary
    @ConfigurationProperties("main.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("main.datasource.hikari")
    DataSource dataSource(final DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @EventListener
    public void printApplicationUrl(final ApplicationStartedEvent event) {
        LoggerFactory.getLogger(FlowsetControlApplication.class).info("Application started at "
                + "http://localhost:"
                + environment.getProperty("local.server.port")
                + Strings.nullToEmpty(environment.getProperty("server.servlet.context-path")));
    }
}
