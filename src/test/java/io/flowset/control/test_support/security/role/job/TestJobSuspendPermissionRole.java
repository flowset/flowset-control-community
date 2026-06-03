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
        name = "Test Permission: job suspend",
        code = TestJobSuspendPermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestJobSuspendPermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-job-suspend-permission";

    @SpecificPolicy(resources = SpecificPermissions.JOB_SUSPEND)
    void jobSuspend();
}
