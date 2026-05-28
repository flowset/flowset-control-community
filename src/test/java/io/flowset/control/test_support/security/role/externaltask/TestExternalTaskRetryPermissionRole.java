/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.externaltask;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Permission: external task retry",
        code = TestExternalTaskRetryPermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestExternalTaskRetryPermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-external-task-retry-permission";

    @SpecificPolicy(resources = SpecificPermissions.EXTERNAL_TASK_RETRY)
    void externalTaskRetry();
}
