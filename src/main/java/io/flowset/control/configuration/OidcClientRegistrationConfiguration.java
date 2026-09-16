/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.List;

/**
 * Registers the {@link ClientRegistrationRepository} built from the {@code spring.security.oauth2.client}
 * properties. Spring Boot auto-configuration does not provide it because the application declares its own
 * {@link ClientRegistrationRepository} beans.
 */
@Configuration
@ConditionalOnProperty(name = "flowset.control.security.login-mode", havingValue = "oidc")
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class OidcClientRegistrationConfiguration {

    @Bean("control_ClientRegistrationRepository")
    public ClientRegistrationRepository inMemoryClientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new OAuth2ClientPropertiesMapper(properties)
                .asClientRegistrations()
                .values()
                .stream()
                .toList();

        return new InMemoryClientRegistrationRepository(registrations);
    }
}
