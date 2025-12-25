package io.flowset.control.service.engine;

import org.springframework.lang.Nullable;

/**
 * Provides functionality to retrieve the tenant ID associated with the current user and engine.
 */
public interface EngineTenantProvider {
    /**
     * Retrieves the tenant ID associated with the current user and should be used for requests to the engine.
     * @return the tenant ID of the current user, or null if no tenant is associated.
     */
    @Nullable
    String getCurrentUserTenantId();
}