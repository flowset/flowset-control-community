/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.auth;

import io.flowset.control.entity.engine.BpmEngine;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Caches {@link ClientRegistration}s for BPM engines.
 */
@Component("control_EngineClientRegistrationRepository")
public class EngineClientRegistrationRepository implements ClientRegistrationRepository {
    private static final Logger log = LoggerFactory.getLogger(EngineClientRegistrationRepository.class);
    protected final DataManager dataManager;

    protected Map<String, ClientRegistration> clientRegistrations = new ConcurrentHashMap<>();

    public EngineClientRegistrationRepository(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return clientRegistrations.computeIfAbsent(registrationId, this::createEngineClientRegistration);
    }

    public ClientRegistration createRegistration(BpmEngine engine, String registrationId) {
        ClientRegistration.Builder builder = ClientRegistrations
                .fromIssuerLocation(StringUtils.trimToEmpty(engine.getOauth2IssuerUri()))
                .clientId(engine.getOauth2ClientId())
                .clientSecret(engine.getOauth2ClientSecret())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .registrationId(registrationId);

        Set<String> scopes = Arrays.stream(StringUtils.defaultString(engine.getOauth2Scope()).split("[,\\s]+"))
                .map(StringUtils::trimToNull)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (!scopes.isEmpty()) {
            builder.scope(scopes);
        }

        return builder.build();
    }

    public void removeRegistration(UUID engineId) {
        log.debug("Evicting client registration for engine with id {}", engineId);
        clientRegistrations.remove(engineId.toString());
    }

    @Nullable
    protected ClientRegistration createEngineClientRegistration(String registrationId) {
        UUID engineId;
        try {
            engineId = UUID.fromString(registrationId);
        } catch (IllegalArgumentException e) {
            //ignore a case if registrationId is not a valid UUID
            return null;
        }
        BpmEngine bpmEngine = dataManager.load(Id.of(engineId, BpmEngine.class))
                .optional()
                .orElse(null);

        if (bpmEngine == null) {
            log.warn("BpmEngine with id {} not found", engineId);
            return null;
        }

        return createRegistration(bpmEngine, engineId.toString());
    }


}
