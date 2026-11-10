/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import feign.FeignException;
import io.flowset.control.entity.EngineConnectionCheckResult;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.property.EngineConnectionCheckProperties;
import io.flowset.control.restsupport.FeignClientCreationContext;
import io.flowset.control.restsupport.FeignClientProvider;
import io.flowset.control.service.engine.EngineConnectionProvider;
import io.flowset.control.service.engine.ZoneOffsetSource;
import io.flowset.control.service.engine.auth.EngineAuthenticator;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.camunda.community.rest.client.api.VersionApiClient;
import org.camunda.community.rest.client.model.VersionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component("control_Camunda7EngineConnectionProvider")
@Slf4j
public class Camunda7EngineConnectionProvider implements EngineConnectionProvider {

    protected static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    protected final FeignClientProvider feignClientProvider;
    protected final EngineConnectionCheckProperties checkProperties;
    protected final EngineAuthenticator engineAuthenticator;
    protected final EngineTimeResolver engineTimeResolver;

    protected final Map<UUID, VersionApiClient> versionClientByEngineId = new ConcurrentHashMap<>();

    public Camunda7EngineConnectionProvider(FeignClientProvider feignClientProvider,
                                            EngineConnectionCheckProperties checkProperties,
                                            EngineAuthenticator engineAuthenticator,
                                            EngineTimeResolver engineTimeResolver) {
        this.feignClientProvider = feignClientProvider;
        this.checkProperties = checkProperties;
        this.engineAuthenticator = engineAuthenticator;
        this.engineTimeResolver = engineTimeResolver;
    }

    @Override
    public void check(BpmEngine engine, EngineConnectionCheckResult result) {
        VersionApiClient versionApiClient = versionClientByEngineId
                .computeIfAbsent(engine.getId(), engineId -> createVersionApiClient(engine));
        try {
            ResponseEntity<VersionDto> response = engineTimeResolver.registerEngineTime(engine.getId(), versionApiClient::getRestAPIVersion);
            if (response.getStatusCode().is2xxSuccessful()) {
                result.setSuccess(true);

                VersionDto versionDto = response.getBody();
                if (versionDto != null) {
                    result.setVersion(versionDto.getVersion());
                } else {
                    log.warn("Empty response is returned while engine version loading, status code {}", response.getStatusCode());
                    result.setVersion("");
                }
            } else {
                log.error("Error while engine version loading, status code {}", response.getStatusCode());
                result.setSuccess(false);
            }
        } catch (FeignException e) {
            log.error("Error while engine version loading: {} ", e.getMessage());
            result.setSuccess(false);
        }
    }

    @Override
    public String getVersion(BpmEngine engine) {
        try {
            VersionApiClient camundaClient = feignClientProvider.createCamundaClient(new FeignClientCreationContext<>(VersionApiClient.class)
                    .setUrl(engine.getBaseUrl())
                    .setRequestInterceptor(engineAuthenticator.createStatelessAuthInterceptor(engine)));

            ResponseEntity<VersionDto> response = engineTimeResolver.registerEngineTime(engine.getId(), camundaClient::getRestAPIVersion);
            if (response.getStatusCode().is2xxSuccessful()) {
                VersionDto versionDto = response.getBody();
                return versionDto != null ? versionDto.getVersion() : "";
            }

            throw new EngineConnectionFailedException(response.getStatusCode().value(), "Unable to get an engine version");
        } catch (FeignException e) {
            log.error("Error on loading the version from engine  '{}'", engine.getBaseUrl(), e);
            throw new EngineConnectionFailedException(e.status(), e.getMessage());
        }
    }

    @Override
    public List<ZoneOffsetSource> getZoneOffsetSources(BpmEngine engine) {
        return List.of(
                new ZoneOffsetSource()
                        .setPath("/deployment?sortBy=deploymentTime&sortOrder=desc&maxResults=1")
                        .setDateField("deploymentTime")
                        .setDateFormatter(DATE_TIME_FORMATTER),
                new ZoneOffsetSource()
                        .setPath("/history/process-instance?sortBy=startTime&sortOrder=desc&maxResults=1")
                        .setDateField("startTime")
                        .setDateFormatter(DATE_TIME_FORMATTER)
        );
    }

    protected VersionApiClient createVersionApiClient(BpmEngine engine) {
        return feignClientProvider.createCamundaClient(new FeignClientCreationContext<>(VersionApiClient.class)
                .setUrl(engine.getBaseUrl())
                .setMaxRetries(checkProperties.getMaxRetries())
                .setRetryTimeout(checkProperties.getRetryTimeout())
                .setConnectTimeout(checkProperties.getConnectTimeout())
                .setReadTimeout(checkProperties.getReadTimeout())
                .setRequestInterceptor(engineAuthenticator.createLockAwareAuthInterceptor(engine)));
    }

    @TransactionalEventListener
    public void onBpmEngineChangedAfterCommit(final EntityChangedEvent<BpmEngine> event) {
        Id<BpmEngine> entityId = event.getEntityId();
        if (event.getType() == EntityChangedEvent.Type.DELETED
                || event.getType() == EntityChangedEvent.Type.UPDATED) {
            versionClientByEngineId.remove((UUID) entityId.getValue());
        }
    }
}
