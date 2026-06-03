/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.engine;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Permission: mark BPM engine as default",
        code = TestEngineMarkAsDefaultPermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestEngineMarkAsDefaultPermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-engine-mark-as-default-permission";

    @SpecificPolicy(resources = SpecificPermissions.ENGINE_MARK_AS_DEFAULT)
    void markAsDefault();
}
