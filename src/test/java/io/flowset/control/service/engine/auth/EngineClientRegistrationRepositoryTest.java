/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.auth;

import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.testcontainers.KeycloakContainer;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class EngineClientRegistrationRepositoryTest extends AbstractIntegrationTest {

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
    EngineClientRegistrationRepository engineClientRegistrationRepository;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM control_bpm_engine");
    }

    @Test
    @DisplayName("Client registration is loaded from BPM engine OAuth2 settings")
    void testGivenExistingOAuth2Engine_whenFindByRegistrationId_thenClientRegistrationIsLoaded() {
        //given
        BpmEngine engine = createOAuth2Engine("control-secret");

        //when
        ClientRegistration registration = engineClientRegistrationRepository.findByRegistrationId(engine.getId().toString());

        //then
        assertThat(registration).isNotNull();
        assertThat(registration.getRegistrationId()).isEqualTo(engine.getId().toString());
        assertThat(registration.getClientId()).isEqualTo("control-test-client");
        assertThat(registration.getClientSecret()).isEqualTo("control-secret");
        assertThat(registration.getAuthorizationGrantType()).isEqualTo(AuthorizationGrantType.CLIENT_CREDENTIALS);
        assertThat(registration.getScopes()).containsExactlyInAnyOrder("openid", "profile");
    }

    @Test
    @DisplayName("Client registration cache is refreshed after explicit removal")
    void testGivenUpdatedOAuth2Engine_whenRegistrationRemoved_thenFindByRegistrationIdReturnsUpdatedSettings() {
        //given
        BpmEngine engine = createOAuth2Engine("control-secret");

        ClientRegistration cachedRegistration = engineClientRegistrationRepository.findByRegistrationId(engine.getId().toString());
        engine.setOauth2ClientSecret("updated-secret");
        dataManager.save(engine);

        engineClientRegistrationRepository.removeRegistration(engine.getId());

        //when
        ClientRegistration refreshedRegistration = engineClientRegistrationRepository.findByRegistrationId(engine.getId().toString());

        //then
        assertThat(cachedRegistration).isNotNull();
        assertThat(refreshedRegistration).isNotNull();
        assertThat(refreshedRegistration).isNotSameAs(cachedRegistration);
        assertThat(refreshedRegistration.getClientSecret()).isEqualTo("updated-secret");
    }

    @Test
    @DisplayName("Client registration lookup returns null for non-engine registration id")
    void testGivenInvalidRegistrationId_whenFindByRegistrationId_thenNullReturned() {
        //given
        String invalidRegistrationId = "not-a-uuid";

        //when
        ClientRegistration registration = engineClientRegistrationRepository.findByRegistrationId(invalidRegistrationId);

        //then
        assertThat(registration).isNull();
    }

    @Test
    @DisplayName("Client registration lookup returns null for missing BPM engine id")
    void testGivenMissingEngineId_whenFindByRegistrationId_thenNullReturned() {
        //given
        String registrationId = UUID.randomUUID().toString();

        //when
        ClientRegistration registration = engineClientRegistrationRepository.findByRegistrationId(registrationId);

        //then
        assertThat(registration).isNull();
    }

    @Test
    @DisplayName("Removing another registration id does not evict cached registration")
    void testGivenCachedRegistration_whenAnotherRegistrationRemoved_thenCachedRegistrationIsStillReturned() {
        //given
        BpmEngine engine = createOAuth2Engine("control-secret");
        ClientRegistration cachedRegistration = engineClientRegistrationRepository.findByRegistrationId(engine.getId().toString());
        engineClientRegistrationRepository.removeRegistration(UUID.randomUUID());

        //when
        ClientRegistration stillCachedRegistration = engineClientRegistrationRepository.findByRegistrationId(engine.getId().toString());

        //then
        assertThat(cachedRegistration).isNotNull();
        assertThat(stillCachedRegistration).isNotNull();
        assertThat(stillCachedRegistration).isSameAs(cachedRegistration);
    }

    @Test
    @DisplayName("Client registration scope is parsed from spaces and commas")
    void testGivenOAuth2ScopeWithDifferentDelimiters_whenFindByRegistrationId_thenScopesParsedCorrectly() {
        //given
        BpmEngine engine = createOAuth2Engine("control-secret", "openid, profile   email");

        //when
        ClientRegistration registration = engineClientRegistrationRepository.findByRegistrationId(engine.getId().toString());

        //then
        assertThat(registration).isNotNull();
        assertThat(registration.getScopes()).containsExactlyInAnyOrder("openid", "profile", "email");
    }

    BpmEngine createOAuth2Engine(String clientSecret) {
        return createOAuth2Engine(clientSecret, "openid profile");
    }

    BpmEngine createOAuth2Engine(String clientSecret, String scope) {
        BpmEngine engine = dataManager.create(BpmEngine.class);
        engine.setName("oauth2-reg-" + UUID.randomUUID().toString().substring(0, 8));
        engine.setType(EngineType.CAMUNDA_7);
        engine.setBaseUrl("http://localhost:8082/engine-rest/");
        engine.setAuthEnabled(true);
        engine.setAuthType(AuthType.OAUTH2);
        engine.setOauth2IssuerUri(keycloakContainer.getIssuerUri());
        engine.setOauth2ClientId("control-test-client");
        engine.setOauth2ClientSecret(clientSecret);
        engine.setOauth2Scope(scope);
        return dataManager.save(engine);
    }
}
