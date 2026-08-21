package io.flowset.control.configuration;

import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import io.jmix.core.JmixOrder;
import io.jmix.oidc.OidcProperties;
import io.jmix.oidc.userinfo.JmixOidcUserService;
import io.jmix.securityflowui.security.AbstractFlowuiWebSecurity;
import io.jmix.securityflowui.security.FlowuiVaadinWebSecurity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

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
public class OidcSecurityConfiguration extends AbstractFlowuiWebSecurity {
    private static final String DEFAULT_LOGIN_PAGE_URL = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;

    protected final JmixOidcUserService jmixOidcUserService;
    protected final ClientRegistrationRepository clientRegistrationRepository;
    protected OidcProperties oidcProperties;

    /**
     * Creates a new OIDC security configuration.
     *
     * @param jmixOidcUserService          the service used to load user information from the OIDC provider
     * @param clientRegistrationRepository the client registration repository used for OIDC logout handling
     */
    public OidcSecurityConfiguration(JmixOidcUserService jmixOidcUserService,
                                     ClientRegistrationRepository clientRegistrationRepository,
                                     OidcProperties oidcProperties) {
        this.jmixOidcUserService = jmixOidcUserService;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oidcProperties = oidcProperties;
    }


    @Override
    protected void configureJmixSpecifics(HttpSecurity http) throws Exception {
        super.configureJmixSpecifics(http);

        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(jmixOidcUserService))
                .successHandler(new VaadinSavedRequestAwareAuthenticationSuccessHandler())
        );

        OidcClientInitiatedLogoutSuccessHandler oidcLogoutHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutHandler.setPostLogoutRedirectUri("{baseUrl}/login");

        http.logout(logout -> logout.logoutSuccessHandler(oidcLogoutHandler));
    }

    @Override
    protected void configureVaadinSpecifics(HttpSecurity http) {
        // Keep Flow navigation aligned with Spring Security: one client goes directly to
        // the provider authorization endpoint, several clients use the generated /login page.
        http.with(VaadinSecurityConfigurer.vaadin(),
                configurer -> configurer.oauth2LoginPage(getOidcLoginUrl(), oidcProperties.getPostLogoutRedirectUri()));
    }

    /**
     * Mirrors the default Spring Security OAuth2 login behavior so that Vaadin navigation
     * access control points unauthenticated users to the same login URL.
     */
    protected String getOidcLoginUrl() {
        if (clientRegistrationRepository instanceof Iterable<?> clientRegistrations) {
            String loginUrl = null;

            for (Object candidate : clientRegistrations) {
                if (!(candidate instanceof ClientRegistration clientRegistration)
                        || !AuthorizationGrantType.AUTHORIZATION_CODE.equals(clientRegistration.getAuthorizationGrantType())) {
                    continue;
                }

                if (loginUrl != null) {
                    return DEFAULT_LOGIN_PAGE_URL;
                }

                loginUrl = "/oauth2/authorization/" + clientRegistration.getRegistrationId();
            }

            if (loginUrl != null) {
                return loginUrl;
            }
        }

        return DEFAULT_LOGIN_PAGE_URL;
    }
}