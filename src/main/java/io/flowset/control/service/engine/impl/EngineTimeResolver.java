package io.flowset.control.service.engine.impl;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.property.EngineProperties;
import io.flowset.control.service.engine.EngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Internal bean manages engines time offsets.<br/>
 * Engine time effectful service calls should be provided via parameter lambda actions.
 */
@RequiredArgsConstructor
@Component("control_EngineTimeResolver")
public class EngineTimeResolver {

    private final ConcurrentMap<UUID, OffsetRecord> offsets = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Boolean> skipMap = new ConcurrentHashMap<>();

    private final EngineService engineService;

    private final EngineProperties engineProperties;

    /**
     * Actualize all registered offset using effectful action (action should call #registerEngineTime(UUID, Supplier) at least once)
     *
     * @param actualizationAction effectful call action
     * @see #registerEngineTime(UUID, Supplier)
     */
    public void actualizeAllRegistered(Consumer<BpmEngine> actualizationAction) {
        for (UUID engineId : offsets.keySet()) {
            actualizeEngineTime(engineId, actualizationAction);
        }
    }

    /**
     * Actualize offset for engine with id using effectful action (action should call #registerEngineTime(UUID, Supplier) at least once)
     *
     * @param engineId            engine
     * @param actualizationAction effectful call action
     */
    public void actualizeEngineTime(UUID engineId, Consumer<BpmEngine> actualizationAction) {
        skipMap.putIfAbsent(engineId, false);

        if (!skipMap.replace(engineId, false, true)) {
            return;
        }

        OffsetRecord record = offsets.get(engineId);
        if (record != null && record.isActualAt(System.currentTimeMillis())) {
            return;
        }

        BpmEngine engine = engineService.findEngineByUuid(engineId);
        if (engine == null) {
            unregisterEngine(engineId);
            return;
        }

        try {
            actualizationAction.accept(engine);

            skipMap.replace(engineId, true, false);
        } catch (Exception e) {
            unregisterEngine(engineId);
        }
    }

    /**
     * Calculate and register engine time offset with single HTTP call transparently.
     *
     * @param engineId    engine id
     * @param requestBody HTTP call (as least extra actions as possible for precise RTT calculating)
     * @param <T>         result type
     * @return HTTP call result
     */
    public <T> ResponseEntity<T> registerEngineTime(UUID engineId, Supplier<ResponseEntity<T>> requestBody) {
        try {
            long requestTime = System.currentTimeMillis();
            ResponseEntity<T> responseEntity = requestBody.get();
            long retrieveTime = System.currentTimeMillis();

            long rtt = (retrieveTime - requestTime);
            long engineServerDate = responseEntity.getHeaders().getDate();
            offsets.put(engineId, new OffsetRecord(
                    computeOffsetInMillis(engineServerDate, rtt, retrieveTime),
                    rtt,
                    retrieveTime,
                    engineProperties.getOffsetLifetimeInMillis()
            ));
            return responseEntity;
        } catch (Exception e) {
            unregisterEngine(engineId);
            throw e;
        }
    }

    /**
     * Remove engine from register.
     *
     * @param engineId engine
     */
    public void unregisterEngine(UUID engineId) {
        skipMap.put(engineId, true);

        offsets.remove(engineId);
        skipMap.remove(engineId);
    }

    /**
     * Check if engine's time offset is still actual.
     *
     * @param engineId engine
     * @return true if offset is still actual
     */
    public boolean isActual(UUID engineId) {
        OffsetRecord offsetRecord = offsets.get(engineId);

        if (offsetRecord == null) {
            return false;
        }

        return offsetRecord.isActualAt(System.currentTimeMillis());
    }

    /**
     * Returns offset registered for engine.
     *
     * @param engineId engine
     * @return offset for engine or null if it is not registered
     */
    @Nullable
    public Long getEngineOffsetInMillis(UUID engineId) {
        if (offsets.containsKey(engineId)) {
            return offsets.get(engineId).offset();
        }

        return null;
    }

    /**
     * Returns engine time computed with registered offset.
     *
     * @param engineId engine
     * @return engine time in millis
     */
    @Nullable
    public Long getEngineTimeInMillis(UUID engineId) {
        if (offsets.containsKey(engineId)) {
            return offsets.get(engineId).offset() + System.currentTimeMillis();
        }

        return null;
    }

    /**
     * Compute server offset.
     *
     * @param engineServerDate server system date from header (truncated to seconds)
     * @param rttInMs          computed rtt in ms
     * @param retrieveTimeInMs retrieveTime in ms
     * @return server offset
     */
    protected long computeOffsetInMillis(long engineServerDate, long rttInMs, long retrieveTimeInMs) {
        long truncatedRetrieveTimeInMs = Instant.ofEpochMilli(retrieveTimeInMs).truncatedTo(ChronoUnit.SECONDS).toEpochMilli();
        long truncatedRtt = Duration.ofMillis(rttInMs).truncatedTo(ChronoUnit.SECONDS).toMillis();

        return engineServerDate + (truncatedRtt / 2) - truncatedRetrieveTimeInMs;
    }

    private record OffsetRecord(long offset, long lastRtt, long recordedTime, long lifetimeInMillis) {

        boolean isActualAt(long timeInMillis) {
            long lifetime = timeInMillis - recordedTime;

            return lifetime < lastRtt || lifetime < lifetimeInMillis;
        }
    }
}
