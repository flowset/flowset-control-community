/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineAuthState;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.testcontainers.KeycloakContainer;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

@ExtendWith(AuthenticatedAsAdmin.class)
@TestPropertySource(properties = {
        "flowset.control.engine.oauth2.lock-enabled=false",
        "flowset.control.engine.oauth2.max-retries=2"
})
public class EngineAuthenticatorLockDisabledTest extends AbstractIntegrationTest {
    static KeycloakContainer<?> keycloakContainer;

    @Autowired
    DataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    EngineAuthenticator engineAuthenticator;

    @BeforeAll
    static void beforeAll() {
        keycloakContainer = new KeycloakContainer<>();
        keycloakContainer.start();
    }

    @AfterAll
    static void afterAll() {
        if (keycloakContainer != null) {
            keycloakContainer.stop();
        }
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM control_engine_auth_state");
        jdbcTemplate.execute("DELETE FROM control_bpm_engine");
    }

    @Test
    @DisplayName("Lock-aware interceptor sets token and does not create state for valid client when lock disabled")
    void givenValidClientCredentials_whenLockAwareInterceptorAppliedAndLockDisabled_thenBearerHeaderIsSetAndStateIsNotCreated() {
        //given
        BpmEngine engine = createOAuth2Engine("control-secret");
        RequestInterceptor interceptor = engineAuthenticator.createLockAwareAuthInterceptor(engine);
        RequestTemplate template = new RequestTemplate();

        //when
        interceptor.apply(template);

        //then
        assertThat(template.headers())
                .containsKey(HttpHeaders.AUTHORIZATION);
        assertThat(template.headers().get(HttpHeaders.AUTHORIZATION))
                .singleElement(STRING)
                .startsWith("Bearer ");

        Optional<EngineAuthState> engineAuthState = findEngineAuthState(engine);

        assertThat(engineAuthState).isEmpty();
    }

    @Test
    @DisplayName("Lock-aware interceptor does not create state for invalid client when lock disabled")
    void givenInvalidClientCredentials_whenLockAwareInterceptorAppliedAndLockDisabled_thenStateIsNotCreated() {
        //given
        BpmEngine engine = createOAuth2Engine("invalid-secret");
        RequestInterceptor interceptor = engineAuthenticator.createLockAwareAuthInterceptor(engine);
        RequestTemplate template = new RequestTemplate();

        //when
        interceptor.apply(template);

        //then
        assertThat(template.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);

        Optional<EngineAuthState> engineAuthState = findEngineAuthState(engine);
        assertThat(engineAuthState).isEmpty();
    }

    @Test
    @DisplayName("Repeated lock-aware OAuth2 failures do not create state when lock disabled")
    void givenInvalidClientCredentials_whenLockAwareInterceptorAppliedRepeatedlyAndLockDisabled_thenStateIsNotCreated() {
        //given
        BpmEngine engine = createOAuth2Engine("invalid-secret");
        RequestInterceptor interceptor = engineAuthenticator.createLockAwareAuthInterceptor(engine);

        //when
        interceptor.apply(new RequestTemplate());
        interceptor.apply(new RequestTemplate());
        interceptor.apply(new RequestTemplate());

        //then
        Optional<EngineAuthState> engineAuthState = findEngineAuthState(engine);
        assertThat(engineAuthState).isEmpty();
    }

    @Test
    @DisplayName("HttpHeaders OAuth2 path does not create state for invalid client when lock disabled")
    void givenInvalidClientCredentials_whenApplyAuthenticationToHttpHeadersAndLockDisabled_thenNoHeaderAndNoState() {
        //given
        BpmEngine engine = createOAuth2Engine("invalid-secret");
        HttpHeaders headers = new HttpHeaders();

        //when
        engineAuthenticator.applyAuthentication(engine, headers);

        //then
        assertThat(headers).doesNotContainKey(HttpHeaders.AUTHORIZATION);

        Optional<EngineAuthState> engineAuthState = findEngineAuthState(engine);
        assertThat(engineAuthState).isEmpty();
    }

    @Test
    @DisplayName("RequestTemplate OAuth2 path does not create state for invalid client when lock disabled")
    void givenInvalidClientCredentials_whenApplyAuthenticationToRequestTemplateAndLockDisabled_thenNoHeaderAndNoState() {
        //given
        BpmEngine engine = createOAuth2Engine("invalid-secret");
        RequestTemplate template = new RequestTemplate();

        //when
        engineAuthenticator.applyAuthentication(engine, template);

        //then
        assertThat(template.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);

        Optional<EngineAuthState> engineAuthState = findEngineAuthState(engine);
        assertThat(engineAuthState).isEmpty();
    }

    @Test
    @DisplayName("Pre-existing locked state does not block token resolution when lock disabled")
    void givenPreLockedState_whenLockAwareInterceptorAppliedWithValidCredentialsAndLockDisabled_thenTokenIsResolved() {
        //given
        BpmEngine engine = createOAuth2Engine("control-secret");
        insertLockedState(engine);

        RequestInterceptor interceptor = engineAuthenticator.createLockAwareAuthInterceptor(engine);
        RequestTemplate template = new RequestTemplate();

        //when
        interceptor.apply(template);

        //then
        assertThat(template.headers()).containsKey(HttpHeaders.AUTHORIZATION);
        assertThat(template.headers().get(HttpHeaders.AUTHORIZATION))
                .singleElement(STRING)
                .startsWith("Bearer ");

        Optional<EngineAuthState> engineAuthState = findEngineAuthState(engine);
        assertThat(engineAuthState).isPresent();
    }

    Optional<EngineAuthState> findEngineAuthState(BpmEngine engine) {
        return dataManager.load(EngineAuthState.class)
                .condition(PropertyCondition.equal("engine.id", engine.getId()))
                .optional();
    }

    BpmEngine createOAuth2Engine(String clientSecret) {
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setName("ld-oauth2-" + UUID.randomUUID().toString().substring(0, 8));
        engine.setType(EngineType.CAMUNDA_7);
        engine.setBaseUrl("http://localhost:8082/engine-rest/");
        engine.setAuthEnabled(true);
        engine.setAuthType(AuthType.OAUTH2);
        engine.setOauth2IssuerUri(keycloakContainer.getIssuerUri());
        engine.setOauth2ClientId("control-test-client");
        engine.setOauth2ClientSecret(clientSecret);
        engine.setOauth2Scope("openid profile");
        return dataManager.save(engine);
    }

    void insertLockedState(BpmEngine engine) {
        EngineAuthState engineAuthState = dataManager.create(EngineAuthState.class);
        engineAuthState.setEngine(engine);
        engineAuthState.setAccessTokenRetries(2);
        engineAuthState.setIsLocked(true);
        dataManager.save(engineAuthState);
    }
}
