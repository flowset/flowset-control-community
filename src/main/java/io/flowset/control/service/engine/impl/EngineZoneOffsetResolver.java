/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.property.EngineConnectionCheckProperties;
import io.flowset.control.property.EngineProperties;
import io.flowset.control.service.engine.EngineConnectionProvider;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.ZoneOffsetSource;
import io.flowset.control.service.engine.auth.EngineAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the time zone offset of the BPM engine server.
 * <p>
 * The engine REST API has no method returning the engine time zone, but it serializes dates with the offset of
 * the engine JVM (e.g. {@code 2026-10-06T16:40:13.533+0400} in Camunda 7). The offset is taken from the requests of
 * {@link EngineConnectionProvider#getZoneOffsetSources(BpmEngine)} of the engine type, e.g. for Camunda 7 from
 * the latest deployment or, if there are no deployments, from the latest historic process instance. Each source
 * declares the format of its dates.
 * <p>
 * Limitations: the engine must contain at least one record the sources return; only the offset is known,
 * not the zone name; the offset is the one valid at the date of the record (daylight saving time may differ);
 * an engine of unknown type has no offset.
 */
@Component("control_EngineZoneOffsetResolver")
public class EngineZoneOffsetResolver {

    protected static final Logger log = LoggerFactory.getLogger(EngineZoneOffsetResolver.class);

    protected final ConcurrentMap<UUID, OffsetRecord> offsets = new ConcurrentHashMap<>();

    protected final EngineService engineService;
    protected final EngineAuthenticator engineAuthenticator;
    protected final EngineProperties engineProperties;
    protected final EngineOperationServiceProvider engineOperationServiceProvider;
    protected final RestTemplate restTemplate;

    public EngineZoneOffsetResolver(EngineService engineService,
                                    EngineAuthenticator engineAuthenticator,
                                    EngineProperties engineProperties,
                                    EngineConnectionCheckProperties connectionCheckProperties,
                                    EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineService = engineService;
        this.engineAuthenticator = engineAuthenticator;
        this.engineProperties = engineProperties;
        this.engineOperationServiceProvider = engineOperationServiceProvider;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectionCheckProperties.getConnectTimeout());
        requestFactory.setReadTimeout(connectionCheckProperties.getReadTimeout());
        this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * Returns the time zone offset of the engine server. The value is cached for
     * {@link EngineProperties#getZoneOffsetLifetimeInMillis()}, an unknown offset is cached as well.
     *
     * @param engineId engine id
     * @return offset of the engine server or empty if it cannot be determined
     */
    public Optional<ZoneOffset> getZoneOffset(UUID engineId) {
        long now = System.currentTimeMillis();
        OffsetRecord record = offsets.get(engineId);
        if (record != null && record.isActualAt(now)) {
            return Optional.ofNullable(record.offset());
        }

        ZoneOffset offset = resolveZoneOffset(engineId);
        offsets.put(engineId, new OffsetRecord(offset, now, engineProperties.getZoneOffsetLifetimeInMillis()));
        return Optional.ofNullable(offset);
    }

    /**
     * Removes the cached offset of the engine, e.g. when the engine settings are changed.
     *
     * @param engineId engine id
     */
    public void invalidate(UUID engineId) {
        offsets.remove(engineId);
    }

    protected ZoneOffset resolveZoneOffset(UUID engineId) {
        BpmEngine engine = engineService.findEngineByUuid(engineId);
        if (engine == null) {
            return null;
        }

        EngineConnectionProvider connectionProvider = engineOperationServiceProvider.getConnectionProvider(engine.getType());
        for (ZoneOffsetSource source : connectionProvider.getZoneOffsetSources(engine)) {
            try {
                String body = requestEngine(engine, source.getPath());
                ZoneOffset offset = extractOffset(body, source);
                if (offset != null) {
                    return offset;
                }
            } catch (Exception e) {
                log.debug("Unable to get zone offset of engine '{}' from '{}': {}", engine.getBaseUrl(), source.getPath(),
                        e.getMessage());
            }
        }
        return null;
    }

    protected String requestEngine(BpmEngine engine, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        engineAuthenticator.applyAuthentication(engine, headers);

        ResponseEntity<String> response = restTemplate.exchange(engine.getBaseUrl() + path, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        return response.getBody();
    }

    /**
     * Extracts the zone offset from the first value of the source date field in a JSON response, parsed with the
     * source date format, e.g. {@code "deploymentTime":"2026-10-06T16:40:13.533+0400"} gives {@code +04:00}.
     *
     * @param body   JSON response of the engine, {@code null} if the response has no body
     * @param source request the response is received for
     * @return offset of the date or {@code null} if the response has no such date field
     */
    protected ZoneOffset extractOffset(String body, ZoneOffsetSource source) {
        if (body == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(source.getDateField()) + "\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(body);
        if (!matcher.find()) {
            return null;
        }
        return OffsetDateTime.parse(matcher.group(1), source.getDateFormatter()).getOffset();
    }

    protected record OffsetRecord(ZoneOffset offset, long recordedTime, long lifetimeInMillis) {
        boolean isActualAt(long timeInMillis) {
            return timeInMillis - recordedTime < lifetimeInMillis;
        }
    }
}
