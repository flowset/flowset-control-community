package io.flowset.control.service.engine.impl;

import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import io.flowset.control.security.UserTimeZoneResolver;
import io.flowset.control.service.engine.EngineTimeService;
import io.flowset.control.service.engine.EngineTimeView;
import io.flowset.control.service.engine.EngineUiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service("control_EngineTimeService")
public class EngineTimeServiceImpl implements EngineTimeService {

    protected static final String ENGINE_TIME_FORMAT_KEY = "engineTimeFormat";
    protected static final String DEFAULT_ENGINE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss 'UTC'xxx";

    private final EngineUiService engineUiService;
    private final EngineTimeResolver engineTimeResolver;
    private final CurrentAuthentication currentAuthentication;
    private final Messages messages;
    private final UserTimeZoneResolver userTimeZoneResolver;
    private final EngineZoneOffsetResolver engineZoneOffsetResolver;

    @Override
    public void actualizeAllRegistered() {
        engineTimeResolver.actualizeAllRegistered(engineUiService::getVersion);
    }

    @Override
    public void actualizeEngineTime(UUID engineId) {
        engineTimeResolver.actualizeEngineTime(engineId, engineUiService::getVersion);
    }

    @Override
    public <T> ResponseEntity<T> registerEngineTime(UUID engineId, Supplier<ResponseEntity<T>> requestBody) {
        return engineTimeResolver.registerEngineTime(engineId, requestBody);
    }

    @Nullable
    @Override
    public EngineTimeView getEngineTimeView(UUID engineId) {
        try {
            Long engineTime = getEngineTimeInMillis(engineId);
            if (engineTime == null) {
                return null;
            }

            Instant instant = Instant.ofEpochMilli(engineTime);
            Locale locale = resolveLocale();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(resolveEngineTimeFormat(locale), locale);

            String userTime = instant.atZone(userTimeZoneResolver.getTimeZone().toZoneId()).format(formatter);

            Optional<ZoneOffset> serverOffset = engineZoneOffsetResolver.getZoneOffset(engineId);
            String serverTime = serverOffset
                    .map(offset -> instant.atOffset(offset).format(formatter))
                    .orElse(userTime);

            return new EngineTimeView(serverTime, userTime);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Returns a pattern for the engine time, e.g. {@code dd.MM.yyyy HH:mm:ss 'UTC'xxx}.
     * The engine time is obtained from the HTTP {@code Date} header with second precision,
     * so the pattern does not contain milliseconds.
     */
    protected String resolveEngineTimeFormat(Locale locale) {
        String pattern = messages.getMessage(ENGINE_TIME_FORMAT_KEY, locale);
        return ENGINE_TIME_FORMAT_KEY.equals(pattern) ? DEFAULT_ENGINE_TIME_FORMAT : pattern;
    }

    protected Locale resolveLocale() {
        return currentAuthentication.isSet() ? currentAuthentication.getLocale() : Locale.getDefault();
    }

    @Nullable
    @Override
    public Long getEngineTimeInMillis(UUID engineId) {
        if (!engineTimeResolver.isActual(engineId)) {
            actualizeEngineTime(engineId);
        }

        return engineTimeResolver.getEngineTimeInMillis(engineId);
    }

    @Nullable
    @Override
    public Long getEngineOffsetInMillis(UUID engineId) {
        try {
            if (!engineTimeResolver.isActual(engineId)) {
                actualizeEngineTime(engineId);
            }

            return engineTimeResolver.getEngineOffsetInMillis(engineId);
        } catch (RuntimeException e) {
            return null;
        }
    }


    @Override
    public void unregisterEngine(UUID engineId) {
        engineTimeResolver.unregisterEngine(engineId);
    }
}
