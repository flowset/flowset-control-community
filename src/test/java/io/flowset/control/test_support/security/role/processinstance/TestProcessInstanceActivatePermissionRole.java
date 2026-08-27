/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processinstance;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Permission: process instance activate",
        code = TestProcessInstanceActivatePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessInstanceActivatePermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-process-instance-activate-permission";

    @SpecificPolicy(resources = SpecificPermissions.PROCESS_INSTANCE_ACTIVATE)
    void processInstanceActivate();
}
