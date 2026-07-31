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
     * Register (or update) engine offset by provided http call.
     *
     * @param engineId    engine id
     * @param requestCall http call that returns response entity
     * @param <T>         response entity type parameter
     * @return response entity from call
     */
    <T> ResponseEntity<T> registerEngineTime(UUID engineId, Supplier<ResponseEntity<T>> requestCall);

    /**
     * Return engine's time formatted as offset date&time.
     *
     * @param engineId engine id
     * @return formatted time or null if engine is not registered
     */
    @Nullable
    String getEngineTimeDefaultFormat(UUID engineId);

    /**
     * Return engine's time if engine is registered.
     *
     * @param engineId engine id
     * @return instant time or null if engine is not registered
     */
    @Nullable
    Long getEngineTime(UUID engineId);

    /**
     * Return engine's time offset if engine is registered.
     *
     * @param engineId engine id
     * @return offset in ms
     */
    @Nullable
    Long getEngineOffset(UUID engineId);


    /**
     * Remove engine registration.
     *
     * @param engineId engine id
     */
    void unregisterEngine(UUID engineId);
}
