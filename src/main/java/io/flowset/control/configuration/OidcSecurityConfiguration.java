package io.flowset.control.configuration;

import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import io.jmix.core.JmixOrder;
import io.jmix.oidc.userinfo.JmixOidcUserService;
import io.jmix.securityflowui.security.FlowuiVaadinWebSecurity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.List;

/**
 * Security configuration for OpenID Connect (OIDC) authentication mode.
 * <p>
 * This configuration is activated automatically when the application property
 * <pre>
 * flowset.control.security.login-mode=oidc
 * </pre>
 * is set.
 * <p>
 * Extends {@link FlowuiVaadinWebSecurity} to integrate with Jmix FlowUI and configure
 * {@link HttpSecurity} for OIDC login and logout flows.
 * <ul>
 *     <li>Configures {@link JmixOidcUserService} for user information retrieval.</li>
 *     <li>Uses {@link VaadinSavedRequestAwareAuthenticationSuccessHandler} as a success handler.</li>
 *     <li>Sets up {@link OidcClientInitiatedLogoutSuccessHandler} to handle logout via the OIDC provider.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
@ConditionalOnProperty(name = "flowset.control.security.login-mode", havingValue = "oidc")
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class OidcSecurityConfiguration extends FlowuiVaadinWebSecurity {

    protected final JmixOidcUserService jmixOidcUserService;
    protected final ObjectProvider<ClientRegistrationRepository> registrationRepositoryObjectProvider;

    /**
     * Creates a new OIDC security configuration.
     *
     * @param jmixOidcUserService          the service used to load user information from the OIDC provider
     * @param registrationRepositoryObjectProvider the client registration repository used for OIDC logout handling
     */
    public OidcSecurityConfiguration(JmixOidcUserService jmixOidcUserService,
                                     ObjectProvider<ClientRegistrationRepository> registrationRepositoryObjectProvider) {
        this.jmixOidcUserService = jmixOidcUserService;
        this.registrationRepositoryObjectProvider = registrationRepositoryObjectProvider;
    }


    /**
     * Configures {@link HttpSecurity} for OIDC authentication and logout.
     *
     * @param http the {@link HttpSecurity} to modify
     * @throws Exception if an error occurs while configuring security
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);

        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(jmixOidcUserService))
                .successHandler(new VaadinSavedRequestAwareAuthenticationSuccessHandler())
        );

        OidcClientInitiatedLogoutSuccessHandler oidcLogoutHandler =
                new OidcClientInitiatedLogoutSuccessHandler(registrationRepositoryObjectProvider.getObject());
        oidcLogoutHandler.setPostLogoutRedirectUri("{baseUrl}/login");

        http.logout(logout -> logout.logoutSuccessHandler(oidcLogoutHandler));
    }

    @Bean("control_ClientRegistrationRepository")
    ClientRegistrationRepository inMemoryClientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new OAuth2ClientPropertiesMapper(properties)
                .asClientRegistrations()
                .values()
                .stream()
                .toList();

        return new InMemoryClientRegistrationRepository(registrations);
    }
}