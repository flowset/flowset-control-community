/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import com.google.common.base.Strings;
import feign.FeignException;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import io.jmix.core.*;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.UiEventPublisher;
import io.flowset.control.entity.EngineConnectionCheckResult;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.event.UserEngineSelectEvent;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.property.EngineConnectionCheckProperties;
import io.flowset.control.restsupport.FeignClientCreationContext;
import io.flowset.control.restsupport.FeignClientProvider;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.EngineUiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.camunda.community.rest.client.api.VersionApiClient;
import org.camunda.community.rest.client.model.VersionDto;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("control_EngineUiService")
@Slf4j
public class EngineUiServiceImpl implements EngineUiService {
    protected final Metadata metadata;
    protected final ObjectProvider<SessionData> sessionDataProvider;
    protected final DataManager dataManager;
    protected final FeignClientProvider feignClientProvider;
    protected final UiEventPublisher uiEventPublisher;
    protected final CurrentAuthentication currentAuthentication;
    protected final EngineConnectionCheckProperties checkProperties;
    protected final EngineService engineService;

    protected Map<UUID, VersionApiClient> versionClientByEngineId = new ConcurrentHashMap<>();

    public EngineUiServiceImpl(Metadata metadata,
                               ObjectProvider<SessionData> sessionDataProvider,
                               FeignClientProvider feignClientProvider,
                               UiEventPublisher uiEventPublisher,
                               CurrentAuthentication currentAuthentication, DataManager dataManager,
                               EngineConnectionCheckProperties checkProperties, EngineService engineService) {
        this.metadata = metadata;
        this.sessionDataProvider = sessionDataProvider;
        this.feignClientProvider = feignClientProvider;
        this.uiEventPublisher = uiEventPublisher;
        this.currentAuthentication = currentAuthentication;
        this.dataManager = dataManager;
        this.checkProperties = checkProperties;
        this.engineService = engineService;
    }


    @Override
    public EngineConnectionCheckResult checkConnection(BpmEngine bpmEngine) {
        EngineConnectionCheckResult result = metadata.create(EngineConnectionCheckResult.class);

        BpmEngine persistedEngine = engineService.findEngineByUuid(bpmEngine.getId());
        if (persistedEngine == null) {
            log.error("There is no engine with id  {}", bpmEngine.getId());
            result.setSuccess(false);
            return result;
        }

        VersionApiClient versionApiClient = versionClientByEngineId
                .computeIfAbsent(bpmEngine.getId(), engineId -> createVersionApiClient(persistedEngine));
        try {
            ResponseEntity<VersionDto> response = versionApiClient.getRestAPIVersion();
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

        return result;
    }

    @Override
    public String getVersion(BpmEngine engine) {
        try {
            VersionApiClient camundaClient = feignClientProvider.createCamundaClient(new FeignClientCreationContext<>(VersionApiClient.class)
                    .setUrl(engine.getBaseUrl())
                    .setRequestInterceptor(createBpmEngineRequestInterceptor(engine)));

            ResponseEntity<VersionDto> response = camundaClient.getRestAPIVersion();
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
    public void selectEngine(BpmEngine engine) {
        String username = currentAuthentication.getUser().getUsername();
        log.debug("User '{}' selects engine '{} (name: '{}', url: '{}')'", username, engine.getId(), engine.getName(), engine.getBaseUrl());
        engineService.setSelectedEngine(engine);
        uiEventPublisher.publishEvent(new UserEngineSelectEvent(this, engine, username));
    }

    protected VersionApiClient createVersionApiClient(BpmEngine engine) {
        return feignClientProvider.createCamundaClient(new FeignClientCreationContext<>(VersionApiClient.class)
                .setUrl(engine.getBaseUrl())
                .setMaxRetries(checkProperties.getMaxRetries())
                .setRetryTimeout(checkProperties.getRetryTimeout())
                .setConnectTimeout(checkProperties.getConnectTimeout())
                .setReadTimeout(checkProperties.getReadTimeout())
                .setRequestInterceptor(createBpmEngineRequestInterceptor(engine)));
    }

    @Nullable
    protected RequestInterceptor createBpmEngineRequestInterceptor(BpmEngine engine) {
        RequestInterceptor requestInterceptor = null;
        if (BooleanUtils.isTrue(engine.getAuthEnabled())) {
            if (engine.getAuthType() == AuthType.BASIC) {
                requestInterceptor = new BasicAuthRequestInterceptor(Strings.nullToEmpty(engine.getBasicAuthUsername()), Strings.nullToEmpty(engine.getBasicAuthPassword()));
            } else if (engine.getAuthType() == AuthType.HTTP_HEADER) {
                requestInterceptor = requestTemplate -> {
                    requestTemplate.header(engine.getHttpHeaderName(), engine.getHttpHeaderValue());
                };
            }
        }
        return requestInterceptor;
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