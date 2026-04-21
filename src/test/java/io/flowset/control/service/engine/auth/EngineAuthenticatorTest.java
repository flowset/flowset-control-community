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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class EngineAuthenticatorTest extends AbstractIntegrationTest {
    static KeycloakContainer<?> keycloakContainer;

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

    @Autowired
    DataManager dataManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EngineAuthenticator engineAuthenticator;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM control_engine_auth_state");
        jdbcTemplate.execute("DELETE FROM control_bpm_engine");
    }

    @Test
    @DisplayName("Stateless interceptor does not update engine lock state on OAuth2 failure")
    void testGivenInvalidClientCredentials_whenStatelessInterceptorApplied_thenEngineStateIsNotUpdated() {
        //given
        BpmEngine engine = createOAuth2Engine("invalid-secret");
        RequestInterceptor interceptor = engineAuthenticator.createStatelessAuthInterceptor(engine);

        //when and then
        assertThatThrownBy(() -> interceptor.apply(new RequestTemplate()))
                .isInstanceOf(OAuth2AuthorizationException.class);

        Optional<EngineAuthState> engineAuthStateOpt = dataManager.load(EngineAuthState.class)
                .condition(PropertyCondition.equal("engine.id", engine.getId()))
                .optional();

        assertThat(engineAuthStateOpt).isEmpty();
    }

    @Test
    @DisplayName("Stateless interceptor sets bearer token for valid client credentials")
    void testGivenValidClientCredentials_whenStatelessInterceptorApplied_thenBearerHeaderIsSet() {
        //given
        BpmEngine engine = createOAuth2Engine("control-secret");
        RequestInterceptor interceptor = engineAuthenticator.createStatelessAuthInterceptor(engine);
        RequestTemplate template = new RequestTemplate();

        //when
        interceptor.apply(template);

        //then
        assertThat(template.headers()).containsKey("Authorization");
        assertThat(template.headers().get("Authorization"))
                .singleElement()
                .asString()
                .startsWith("Bearer ");
    }

    @Test
    @DisplayName("Sets basic auth header for HttpHeaders")
    void testGivenBasicAuthEngine_whenApplyAuthenticationToHttpHeaders_thenBasicHeaderIsSet() {
        //given
        BpmEngine engine = createBasicAuthEngine("john", "secret");
        HttpHeaders headers = new HttpHeaders();

        //when
        engineAuthenticator.applyAuthentication(engine, headers);

        //then
        String encodedCredentials = Base64.getEncoder()
                .encodeToString("john:secret".getBytes(StandardCharsets.UTF_8));

        assertThat(headers)
                .containsEntry("Authorization", List.of("Basic " + encodedCredentials));

    }

    @Test
    @DisplayName("Sets basic auth header for RequestTemplate")
    void testGivenBasicAuthEngine_whenApplyAuthenticationToRequestTemplate_thenBasicHeaderIsSet() {
        //given
        BpmEngine engine = createBasicAuthEngine("john", "secret");
        RequestTemplate template = new RequestTemplate();

        //when
        engineAuthenticator.applyAuthentication(engine, template);

        //then
        String encodedCredentials = Base64.getEncoder().encodeToString("john:secret"
                .getBytes(StandardCharsets.UTF_8));

        assertThat(template.headers())
                .containsEntry(HttpHeaders.AUTHORIZATION, List.of("Basic " + encodedCredentials));
    }

    @Test
    @DisplayName("Sets custom header for HttpHeaders")
    void testGivenHttpHeaderEngine_whenApplyAuthenticationToHttpHeaders_thenCustomHeaderIsSet() {
        //given
        BpmEngine engine = createHttpHeaderEngine("X-Engine-Token", "token-123");
        HttpHeaders headers = new HttpHeaders();

        //when
        engineAuthenticator.applyAuthentication(engine, headers);

        //then
        assertThat(headers)
                .containsEntry("X-Engine-Token", List.of("token-123"));
    }

    @Test
    @DisplayName("Sets custom header for RequestTemplate")
    void testGivenHttpHeaderEngine_whenApplyAuthenticationToRequestTemplate_thenCustomHeaderIsSet() {
        //given
        BpmEngine engine = createHttpHeaderEngine("X-Engine-Token", "token-123");
        RequestTemplate template = new RequestTemplate();

        //when
        engineAuthenticator.applyAuthentication(engine, template);

        //then
        assertThat(template.headers())
                .containsEntry("X-Engine-Token", List.of("token-123"));

    }

    @Test
    @DisplayName("No auth headers are set for HttpHeaders when authentication is disabled")
    void testGivenAuthDisabledEngine_whenApplyAuthenticationToHttpHeaders_thenHeadersAreNotChanged() {
        //given
        BpmEngine engine = createBasicAuthEngine("john", "secret");
        engine.setAuthEnabled(false);
        HttpHeaders headers = new HttpHeaders();

        //when
        engineAuthenticator.applyAuthentication(engine, headers);

        //then
        assertThat(headers).doesNotContainKey("Authorization");
    }

    @Test
    @DisplayName("No auth headers are set for RequestTemplate when authentication is disabled")
    void testGivenAuthDisabledEngine_whenApplyAuthenticationToRequestTemplate_thenHeadersAreNotChanged() {
        //given
        BpmEngine engine = createBasicAuthEngine("john", "secret");
        engine.setAuthEnabled(false);
        RequestTemplate template = new RequestTemplate();

        //when
        engineAuthenticator.applyAuthentication(engine, template);

        //then
        assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    @Test
    @DisplayName("No auth headers are set for HttpHeaders when auth type is null")
    void testGivenAuthTypeIsNull_whenApplyAuthenticationToHttpHeaders_thenHeadersAreNotChanged() {
        //given
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setAuthEnabled(true);
        engine.setAuthType(null);
        HttpHeaders headers = new HttpHeaders();

        //when
        engineAuthenticator.applyAuthentication(engine, headers);

        //then
        assertThat(headers).doesNotContainKey("Authorization");
    }

    @Test
    @DisplayName("No auth headers are set for RequestTemplate when auth type is null")
    void testGivenAuthTypeIsNull_whenApplyAuthenticationToRequestTemplate_thenHeadersAreNotChanged() {
        //given
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setAuthEnabled(true);
        engine.setAuthType(null);
        RequestTemplate template = new RequestTemplate();

        //when
        engineAuthenticator.applyAuthentication(engine, template);

        //then
        assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    BpmEngine createOAuth2Engine(String clientSecret) {
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setName("oauth2-" + UUID.randomUUID().toString().substring(0, 8));
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

    BpmEngine createBasicAuthEngine(String username, String password) {
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setName("basic-" + UUID.randomUUID().toString().substring(0, 8));
        engine.setType(EngineType.CAMUNDA_7);
        engine.setBaseUrl("http://localhost:8082/engine-rest/");
        engine.setAuthEnabled(true);
        engine.setAuthType(AuthType.BASIC);
        engine.setBasicAuthUsername(username);
        engine.setBasicAuthPassword(password);
        return dataManager.save(engine);
    }

    BpmEngine createHttpHeaderEngine(String headerName, String headerValue) {
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setName("header-" + UUID.randomUUID().toString().substring(0, 8));
        engine.setType(EngineType.CAMUNDA_7);
        engine.setBaseUrl("http://localhost:8082/engine-rest/");
        engine.setAuthEnabled(true);
        engine.setAuthType(AuthType.HTTP_HEADER);
        engine.setHttpHeaderName(headerName);
        engine.setHttpHeaderValue(headerValue);
        return dataManager.save(engine);
    }
}
