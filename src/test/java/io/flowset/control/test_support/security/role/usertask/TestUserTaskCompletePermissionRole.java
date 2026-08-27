/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.usertask;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Permission: user task complete",
        code = TestUserTaskCompletePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestUserTaskCompletePermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-user-task-complete-permission";

    @SpecificPolicy(resources = SpecificPermissions.USER_TASK_COMPLETE)
    void userTaskComplete();
}
