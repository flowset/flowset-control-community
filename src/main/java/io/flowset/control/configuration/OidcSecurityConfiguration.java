package io.flowset.control.configuration;

import io.jmix.core.JmixOrder;
import io.jmix.oidc.OidcVaadinWebSecurity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Security configuration for OpenID Connect (OIDC) authentication mode.
 * <p>
 * This configuration is activated automatically when the application property
 * <pre>
 * flowset.control.security.login-mode=oidc
 * </pre>
 * is set.
 * <p>
 * Extends {@link OidcVaadinWebSecurity} to integrate with Jmix FlowUI.
 */
@Configuration
@EnableWebSecurity
@Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
@ConditionalOnProperty(name = "flowset.control.security.login-mode", havingValue = "oidc")
public class OidcSecurityConfiguration extends OidcVaadinWebSecurity {

}