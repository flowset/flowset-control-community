package io.flowset.control.service.engine;

import org.springframework.http.ResponseEntity;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * <p>
 * Service provided engine system time. Uses date header from response entity.
 * Wraps existing api or rest calls.
 * </p>
 */
public interface EngineTimeService {

    /**
     * Actualize offsets for each registered engine.
     */
    void actualizeAllRegistered();

    /**
     * Actualize offset for engine with id.
     *
     * @param engineId engine id
     */
    void actualizeEngineTime(UUID engineId);

    /**
     * Register (or update) engine offset by provided HTTP call.
     *
     * @param engineId    engine id
     * @param requestCall HTTP call that returns response entity
     * @param <T>         response entity type parameter
     * @return response entity from call
     */
    <T> ResponseEntity<T> registerEngineTime(UUID engineId, Supplier<ResponseEntity<T>> requestCall);

    /**
     * Returns the current engine time formatted for display (e.g. {@code 06.10.2026 22:07:41 UTC+04:00}):
     * in the time zone of the engine server and in the time zone of the current user.
     * The engine time is obtained from the HTTP {@code Date} header with second precision,
     * so milliseconds are not displayed.
     *
     * @param engineId engine id
     * @return formatted engine time or null if the engine is not available
     */
    @Nullable
    EngineTimeView getEngineTimeView(UUID engineId);

    /**
     * Return engine's time if engine is registered.
     *
     * @param engineId engine id
     * @return instant time or null if engine is not registered
     */
    @Nullable
    Long getEngineTimeInMillis(UUID engineId);

    /**
     * Return engine's time offset if engine is registered.
     *
     * @param engineId engine id
     * @return offset in ms
     */
    @Nullable
    Long getEngineOffsetInMillis(UUID engineId);


    /**
     * Remove engine registration.
     *
     * @param engineId engine id
     */
    void unregisterEngine(UUID engineId);
}
