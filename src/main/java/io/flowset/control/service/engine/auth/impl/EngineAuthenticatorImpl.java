/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.auth.impl;

import com.google.common.base.Strings;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.exception.AccessTokenConnectException;
import io.flowset.control.property.EngineOAuth2Properties;
import io.flowset.control.service.engine.auth.EngineAuthStateService;
import io.flowset.control.service.engine.auth.EngineAuthenticator;
import io.flowset.control.service.engine.auth.EngineClientRegistrationRepository;
import io.jmix.core.EntityStates;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Function;

import static io.flowset.control.util.ExceptionUtils.isConnectionError;

@Component("control_EngineAuthenticator")
@AllArgsConstructor
public class EngineAuthenticatorImpl implements EngineAuthenticator {
    protected static final Logger log = LoggerFactory.getLogger(EngineAuthenticatorImpl.class);
    public static final String CONTROL_SYSTEM_USER = "flowset-control-engine-user";

    protected final OAuth2AuthorizedClientManager authorizedClientManager;
    protected final OAuth2AuthorizedClientService oauth2AuthorizedClientService;
    protected final OAuth2AuthorizedClientProvider oauth2AuthorizedClientProvider;

    protected final EngineClientRegistrationRepository engineClientRegistrationRepository;
    protected final EngineAuthStateService engineAuthStateService;
    protected final EntityStates entityStates;
    protected final EngineOAuth2Properties engineOAuth2Properties;

    @Override
    public void applyAuthentication(BpmEngine engine, HttpHeaders headers) {
        if (!isAuthenticationRequired(engine)) {
            return;
        }

        switch (engine.getAuthType()) {
            case BASIC -> headers.setBasicAuth(Strings.nullToEmpty(engine.getBasicAuthUsername()),
                    Strings.nullToEmpty(engine.getBasicAuthPassword()));
            case HTTP_HEADER -> headers.set(engine.getHttpHeaderName(), engine.getHttpHeaderValue());
            case OAUTH2 -> {
                String accessToken = obtainAccessToken(engine);
                if (accessToken != null) {
                    headers.setBearerAuth(accessToken);
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void applyAuthentication(BpmEngine engine, RequestTemplate template) {
        if (!isAuthenticationRequired(engine)) {
            return;
        }

        switch (engine.getAuthType()) {
            case BASIC -> {
                String credentials = Strings.nullToEmpty(engine.getBasicAuthUsername()) + ':'
                        + Strings.nullToEmpty(engine.getBasicAuthPassword());
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                template.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
            }
            case HTTP_HEADER -> template.header(engine.getHttpHeaderName(), engine.getHttpHeaderValue());
            case OAUTH2 -> {
                String accessToken = obtainAccessToken(engine);
                if (accessToken != null) {
                    template.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                }
            }
            default -> {
            }
        }
    }

    @Override
    public RequestInterceptor createLockAwareAuthInterceptor(BpmEngine engine) {
        return buildAuthInterceptor(engine, this::obtainAccessToken);
    }

    @Override
    public RequestInterceptor createStatelessAuthInterceptor(BpmEngine engine) {
        return buildAuthInterceptor(engine, bpmEngine -> {
            try {
                return authorizeWithoutClientCache(bpmEngine);
            } catch (Exception e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (isConnectionError(rootCause)) {
                    throw new AccessTokenConnectException(e.getMessage(), e);
                }
                throw e;
            }
        });
    }

    protected RequestInterceptor buildAuthInterceptor(BpmEngine engine, Function<BpmEngine, String> accessTokenResolver) {
        return template -> {
            if (!isAuthenticationRequired(engine)) {
                return;
            }

            switch (engine.getAuthType()) {
                case BASIC -> {
                    String credentials = StringUtils.defaultString(engine.getBasicAuthUsername()) + ':'
                            + StringUtils.defaultString(engine.getBasicAuthPassword());
                    String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
                    template.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
                }
                case HTTP_HEADER -> template.header(engine.getHttpHeaderName(), engine.getHttpHeaderValue());
                case OAUTH2 -> {
                    String accessToken = accessTokenResolver.apply(engine);
                    if (accessToken != null) {
                        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                    }
                }
                default -> {
                }
            }
        };
    }

    @Nullable
    protected String authorizeWithoutClientCache(BpmEngine engine) {
        String registrationId = "tmp-" + engine.getId().toString();
        ClientRegistration clientRegistration = engineClientRegistrationRepository.createRegistration(engine, registrationId);

        OAuth2AuthorizationContext authorizationContext = OAuth2AuthorizationContext.withClientRegistration(clientRegistration)
                .principal(createAuthentication(CONTROL_SYSTEM_USER))
                .build();

        OAuth2AuthorizedClient authorizedClient = oauth2AuthorizedClientProvider.authorize(authorizationContext);
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            return null;
        }
        return authorizedClient.getAccessToken().getTokenValue();
    }

    protected static Authentication createAuthentication(final String principalName) {
        Assert.hasText(principalName, "principalName cannot be empty");
        return new AbstractAuthenticationToken(null) {

            @Override
            public Object getCredentials() {
                return "";
            }

            @Override
            public Object getPrincipal() {
                return principalName;
            }

        };
    }

    protected boolean isAuthenticationRequired(BpmEngine engine) {
        return BooleanUtils.isTrue(engine.getAuthEnabled()) && engine.getAuthType() != null;
    }

    @Nullable
    protected String obtainAccessToken(BpmEngine engine) {
        UUID engineId = engine.getId();
        if (entityStates.isNew(engine)) {
            return authorizeWithoutClientCache(engine);
        }

        if (engineOAuth2Properties.isLockEnabled()) {
            return authorizeWithEngineLockCheck(engine);
        }

        String registrationId = engineId.toString();
        try {
            return authorizeWithClientCache(registrationId);
        } catch (Exception e) {
            log.error("Unable to resolve access token for engine '{}', error: {}", engineId, e.getMessage());
            return null;
        }
    }

    @Nullable
    protected String authorizeWithEngineLockCheck(BpmEngine engine) {
        UUID engineId = engine.getId();
        if (engineAuthStateService.isLocked(engineId)) {
            log.warn("OAuth2 token obtaining is locked for BPM engine '{}'", engineId);
            return null;
        }

        String registrationId = engineId.toString();
        try {
            String token = authorizeWithClientCache(registrationId);
            engineAuthStateService.unlock(engineId);
            return token;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (e instanceof OAuth2AuthorizationException || isConnectionError(rootCause)) {
                log.error("Unable to obtain access token for engine '{}', error: {}", engineId, e.getMessage());
                engineAuthStateService.registerAccessTokenFailure(engine);
            } else {
                log.error("Unable to obtain access token for engine '{}'", engineId, e);
            }
            return null;
        }
    }

    protected void invalidateOAuthClient(UUID engineId) {
        oauth2AuthorizedClientService.removeAuthorizedClient(engineId.toString(), CONTROL_SYSTEM_USER);
        engineClientRegistrationRepository.removeRegistration(engineId);
    }

    @Nullable
    protected String authorizeWithClientCache(String registrationId) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                        .principal(CONTROL_SYSTEM_USER)
                        .build()
        );

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            log.warn("Failed to obtain OAuth2 access token for engine {}", registrationId);
            return null;
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }

    @TransactionalEventListener
    public void onBpmEngineChangedAfterCommit(final EntityChangedEvent<BpmEngine> event) {
        Id<BpmEngine> entityId = event.getEntityId();
        UUID engineId = (UUID) entityId.getValue();

        EntityChangedEvent.Type type = event.getType();
        if (type == EntityChangedEvent.Type.DELETED || type == EntityChangedEvent.Type.UPDATED) {
            invalidateOAuthClient(engineId);
        }
    }
}
