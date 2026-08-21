package io.flowset.control.service.engine.impl;

import io.jmix.multitenancy.core.TenantProvider;
import io.flowset.control.service.engine.EngineTenantProvider;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component("control_EngineTenantProvider")
public class EngineTenantProviderImpl implements EngineTenantProvider {

    protected final TenantProvider tenantProvider;

    public EngineTenantProviderImpl(TenantProvider tenantProvider) {
        this.tenantProvider = tenantProvider;
    }

    @Override
    @Nullable
    public String getCurrentUserTenantId() {
        String currentUserTenantId = tenantProvider.getCurrentUserTenantId();
        if (currentUserTenantId == null || Strings.CS.equals(currentUserTenantId, TenantProvider.NO_TENANT)) {
            return null;
        }
        return currentUserTenantId;
    }
}