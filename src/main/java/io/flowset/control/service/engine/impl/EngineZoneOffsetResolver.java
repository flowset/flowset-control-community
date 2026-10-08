/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine.impl;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.property.EngineConnectionCheckProperties;
import io.flowset.control.property.EngineProperties;
import io.flowset.control.service.engine.EngineService;
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
import java.time.format.DateTimeFormatter;
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
 * the engine JVM (e.g. {@code 2026-10-06T16:40:13.533+0400}). The offset is taken from the latest deployment
 * or, if there are no deployments, from the latest historic process instance.
 * <p>
 * Limitations: the engine must contain at least one deployment or process instance; only the offset is known,
 * not the zone name; the offset is the one valid at the date of the record (daylight saving time may differ).
 */
@Component("control_EngineZoneOffsetResolver")
public class EngineZoneOffsetResolver {

    protected static final Logger log = LoggerFactory.getLogger(EngineZoneOffsetResolver.class);

    protected static final DateTimeFormatter ENGINE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * Requests used to obtain a date serialized by the engine, in order of priority.
     */
    protected static final List<OffsetSource> OFFSET_SOURCES = List.of(
            new OffsetSource("/deployment?sortBy=deploymentTime&sortOrder=desc&maxResults=1", "deploymentTime"),
            new OffsetSource("/history/process-instance?sortBy=startTime&sortOrder=desc&maxResults=1", "startTime")
    );

    protected final ConcurrentMap<UUID, OffsetRecord> offsets = new ConcurrentHashMap<>();

    protected final EngineService engineService;
    protected final EngineAuthenticator engineAuthenticator;
    protected final EngineProperties engineProperties;
    protected final RestTemplate restTemplate;

    public EngineZoneOffsetResolver(EngineService engineService,
                                    EngineAuthenticator engineAuthenticator,
                                    EngineProperties engineProperties,
                                    EngineConnectionCheckProperties connectionCheckProperties) {
        this.engineService = engineService;
        this.engineAuthenticator = engineAuthenticator;
        this.engineProperties = engineProperties;

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

        for (OffsetSource source : OFFSET_SOURCES) {
            try {
                String body = requestEngine(engine, source.path());
                ZoneOffset offset = extractOffset(body, source.dateField());
                if (offset != null) {
                    return offset;
                }
            } catch (Exception e) {
                log.debug("Unable to get zone offset of engine '{}' from '{}': {}", engine.getBaseUrl(), source.path(),
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
     * Extracts the zone offset from the first value of the date field in a JSON response,
     * e.g. {@code "deploymentTime":"2026-10-06T16:40:13.533+0400"} gives {@code +04:00}.
     */
    protected ZoneOffset extractOffset(String body, String dateField) {
        if (body == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(dateField) + "\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
        if (!matcher.find()) {
            return null;
        }
        return OffsetDateTime.parse(matcher.group(1), ENGINE_DATE_TIME_FORMATTER).getOffset();
    }

    protected record OffsetSource(String path, String dateField) {
    }

    protected record OffsetRecord(ZoneOffset offset, long recordedTime, long lifetimeInMillis) {
        boolean isActualAt(long timeInMillis) {
            return timeInMillis - recordedTime < lifetimeInMillis;
        }
    }
}
