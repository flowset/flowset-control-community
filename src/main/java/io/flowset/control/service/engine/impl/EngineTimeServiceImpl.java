package io.flowset.control.service.engine.impl;

import io.flowset.control.service.engine.EngineTimeService;
import io.flowset.control.service.engine.EngineUiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service("control_EngineTimeService")
public class EngineTimeServiceImpl implements EngineTimeService {

    private final EngineUiService engineUiService;
    private final EngineTimeBean engineTimeBean;

    @Override
    public void actualizeAllRegistered() {
        engineTimeBean.actualizeAllRegistered(engineUiService::getVersion);
    }

    @Override
    public void actualizeEngineTime(UUID engineId) {
        engineTimeBean.actualizeEngineTime(engineId, engineUiService::getVersion);
    }

    @Override
    public <T> ResponseEntity<T> registerEngineTime(UUID engineId, Supplier<ResponseEntity<T>> requestBody) {
        return engineTimeBean.registerEngineTime(engineId, requestBody);
    }

    @Override
    public String getEngineTimeDefaultFormat(UUID engineId) {
        try {
            Long engineTime = getEngineTime(engineId);
            if (engineTime != null) {
                OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(engineTime),
                        ZoneId.systemDefault()
                );
                return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }

            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public Long getEngineTime(UUID engineId) {
        if (!engineTimeBean.isActual(engineId)) {
            actualizeEngineTime(engineId);
        }

        return engineTimeBean.getEngineTime(engineId);
    }

    @Nullable
    @Override
    public Long getEngineOffset(UUID engineId) {
        try {
            if (!engineTimeBean.isActual(engineId)) {
                actualizeEngineTime(engineId);
            }

            return engineTimeBean.getEngineOffset(engineId);
        } catch (RuntimeException e) {
            return null;
        }
    }


    @Override
    public void unregisterEngine(UUID engineId) {
        engineTimeBean.unregisterEngine(engineId);
    }
}
