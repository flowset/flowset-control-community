/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.job;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Permission: job activate",
        code = TestJobActivatePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestJobActivatePermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-job-activate-permission";

    @SpecificPolicy(resources = SpecificPermissions.JOB_ACTIVATE)
    void jobActivate();
}
