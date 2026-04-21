/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.configuration;

import io.jmix.core.JmixOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.List;

@Configuration
@Order(JmixOrder.HIGHEST_PRECEDENCE + 200)
public class OAuth2Configuration {

    @Bean("control_OAuth2AuthorizedClientManager")
    public OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                       OAuth2AuthorizedClientProvider authorizedClientProvider,
                                                                       OAuth2AuthorizedClientService clientService) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager authClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);

        authClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authClientManager;
    }

    @Bean("control_OAuth2AuthorizedClientProvider")
    public OAuth2AuthorizedClientProvider oAuth2AuthorizedClientProvider() {
        return OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials(builder ->
                        builder.accessTokenResponseClient(new RestClientClientCredentialsTokenResponseClient()))
                .build();
    }
}
