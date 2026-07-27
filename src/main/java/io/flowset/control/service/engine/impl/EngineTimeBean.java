package io.flowset.control.service.engine.impl;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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

    @Value("${flowset.control.engine.offset-lifetime-in-millis:1000}")
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
        } catch (RuntimeException e) {
            unregisterEngine(engineId);
        }
    }

    public <T> ResponseEntity<T> registerEngineTime(UUID engineId, Supplier<ResponseEntity<T>> requestBody) {
        long beforeTime = System.currentTimeMillis();

        try {
            ResponseEntity<T> responseEntity = requestBody.get();

            long currentTime = System.currentTimeMillis();
            long currentRtt = currentTime - beforeTime;

            long engineServerDate = responseEntity.getHeaders().getDate();

            offsets.put(engineId, new OffsetRecord(
                    engineServerDate + (currentRtt / 2) - currentTime,
                    currentRtt,
                    currentTime,
                    offsetLifetimeInMillis
            ));
            return responseEntity;
        } catch (RuntimeException e) {
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
            return offsets.get(engineId).offset + System.currentTimeMillis();
        }

        return null;
    }

    private record OffsetRecord(long offset, long lastRtt, long recordedTime, long lifetimeInMillis) {

        boolean isActualAt(long timeInMillis) {
            long lifetime = timeInMillis - recordedTime;

            return lifetime < lastRtt || lifetime < lifetimeInMillis;
        }
    }
}
