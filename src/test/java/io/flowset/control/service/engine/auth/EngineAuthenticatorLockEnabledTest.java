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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

@ExtendWith(AuthenticatedAsAdmin.class)
@TestPropertySource(properties = {
        "flowset.control.engine.oauth2.lock-enabled=true",
        "flowset.control.engine.oauth2.max-retries=2"
})
public class EngineAuthenticatorLockEnabledTest extends AbstractIntegrationTest {
    private static KeycloakContainer<?> keycloakContainer;


    @Autowired
    DataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    EngineAuthenticator engineAuthenticator;
    @Autowired
    EngineAuthStateService engineAuthStateService;

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
    @DisplayName("Lock-aware interceptor sets bearer token for valid client credentials")
    void testGivenValidClientCredentials_whenLockAwareInterceptorApplied_thenBearerHeaderIsSet() {
        //given
        BpmEngine engine = createOAuth2Engine("control-secret");
        RequestInterceptor interceptor = engineAuthenticator.createLockAwareAuthInterceptor(engine);
        RequestTemplate template = new RequestTemplate();

        //when
        interceptor.apply(template);

        //then
        assertThat(template.headers()).containsKey("Authorization");
        
        assertThat(template.headers().get("Authorization"))
                .singleElement(STRING)
                .startsWith("Bearer ");

        Optional<EngineAuthState> actualState = findEngineAuthState(engine);
        assertThat(actualState).isEmpty();
    }

    @Test
    @DisplayName("Lock-aware interceptor locks engine after configured OAuth2 failures")
    void testGivenInvalidClientCredentials_whenLockAwareInterceptorAppliedTwice_thenEngineStateIsLocked() {
        //given
        BpmEngine engine = createOAuth2Engine("invalid-secret");
        RequestInterceptor interceptor = engineAuthenticator.createLockAwareAuthInterceptor(engine);

        //when
        interceptor.apply(new RequestTemplate());
        interceptor.apply(new RequestTemplate());

        //then
        Optional<EngineAuthState> actualState = findEngineAuthState(engine);
        assertThat(actualState).isNotEmpty()
                .get()
                .satisfies(state -> {
                    assertThat(state.getEngine()).isEqualTo(engine);
                    assertThat(state.getAccessTokenRetries()).isEqualTo(2);
                    assertThat(state.getIsLocked()).isTrue();
                });

        //when
        interceptor.apply(new RequestTemplate());

        //then
        Optional<EngineAuthState> actualUnchangedState = findEngineAuthState(engine);
        assertThat(actualUnchangedState).isNotEmpty()
                .get()
                .satisfies(state -> {
                    assertThat(state.getAccessTokenRetries()).isEqualTo(2);
                    assertThat(state.getIsLocked()).isTrue();
                });
    }

    @Test
    @DisplayName("Lock-aware interceptor can lock state again after unlock without optimistic lock issues")
    void testGivenLockedThenUnlockedState_whenLockAwareInterceptorLocksAgain_thenNoOptimisticLockAndStateLocked() {
        //given
        BpmEngine engine = createOAuth2Engine("invalid-secret");
        RequestInterceptor interceptor = engineAuthenticator.createLockAwareAuthInterceptor(engine);

        //when
        interceptor.apply(new RequestTemplate());
        interceptor.apply(new RequestTemplate());

        //then
        Optional<EngineAuthState> initiallyLockedState = findEngineAuthState(engine);
        assertThat(initiallyLockedState).isNotEmpty()
                .get()
                .satisfies(state -> {
                    assertThat(state.getAccessTokenRetries()).isEqualTo(2);
                    assertThat(state.getIsLocked()).isTrue();
                });

        //when
        engineAuthStateService.unlock(engine.getId());

        //then
        Optional<EngineAuthState> unlockedState = findEngineAuthState(engine);
        assertThat(unlockedState).isEmpty();

        //when
        interceptor.apply(new RequestTemplate());
        assertThatCode(() -> interceptor.apply(new RequestTemplate())).doesNotThrowAnyException();

        //then
        Optional<EngineAuthState> relockedState = findEngineAuthState(engine);
        assertThat(relockedState).isNotEmpty()
                .get()
                .satisfies(state -> {
                    assertThat(state.getAccessTokenRetries()).isEqualTo(2);
                    assertThat(state.getIsLocked()).isTrue();
                });
    }

    BpmEngine createOAuth2Engine(String clientSecret) {
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setName("le-oauth2-" + UUID.randomUUID().toString().substring(0, 8));
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
    
    Optional<EngineAuthState> findEngineAuthState(BpmEngine engine) {
        return dataManager.load(EngineAuthState.class)
                .condition(PropertyCondition.equal("engine.id", engine.getId()))
                .optional();
    }

}
