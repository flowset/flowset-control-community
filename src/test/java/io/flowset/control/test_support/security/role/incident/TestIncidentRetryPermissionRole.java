/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.incident;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Permission: incident retry",
        code = TestIncidentRetryPermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestIncidentRetryPermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-incident-retry-permission";

    @SpecificPolicy(resources = SpecificPermissions.INCIDENT_RETRY)
    void incidentRetry();
}
