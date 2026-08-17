package io.flowset.control.service.engine.impl;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Component("control_EngineTimeBean")
public class EngineTimeBean {

    private final ConcurrentMap<UUID, OffsetRecord> offsets = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Boolean> skipMap = new ConcurrentHashMap<>();

    private final EngineService engineService;

    @Value("${flowset.control.engine.offset-lifetime-in-millis:10000}")
    private Long offsetLifetimeInMillis;

    public void actualizeAllRegistered(Consumer<BpmEngine> actualizationAction) {
        for (UUID engineId : offsets.keySet()) {
            actualizeEngineTime(engineId, actualizationAction);
        }
    }

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

    public <T> ResponseEntity<T> registerEngineTime(UUID engineId, Supplier<ResponseEntity<T>> requestBody) {
        try {
            long requestTime = System.currentTimeMillis();
            ResponseEntity<T> responseEntity = requestBody.get();
            long retrieveTime = System.currentTimeMillis();

            long rtt = (retrieveTime - requestTime);
            long engineServerDate = responseEntity.getHeaders().getDate();
            offsets.put(engineId, new OffsetRecord(
                    computeOffset(engineServerDate, rtt, retrieveTime),
                    rtt,
                    retrieveTime,
                    offsetLifetimeInMillis
            ));
            return responseEntity;
        } catch (Exception e) {
            unregisterEngine(engineId);
            throw e;
        }
    }

    public void unregisterEngine(UUID engineId) {
        skipMap.put(engineId, true);

        offsets.remove(engineId);
        skipMap.remove(engineId);
    }

    public boolean isActual(UUID engineId) {
        OffsetRecord offsetRecord = offsets.get(engineId);

        if (offsetRecord == null) {
            return false;
        }

        return offsetRecord.isActualAt(System.currentTimeMillis());
    }

    public Long getEngineTime(UUID engineId) {
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
    protected long computeOffset(long engineServerDate, long rttInMs, long retrieveTimeInMs) {
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
