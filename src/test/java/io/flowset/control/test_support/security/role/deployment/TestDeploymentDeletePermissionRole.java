/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.deployment;

import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Permission: deployment delete",
        code = TestDeploymentDeletePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestDeploymentDeletePermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-deployment-delete-permission";

    @EntityPolicy(entityName = "*", actions = EntityPolicyAction.DELETE)
    void deploymentDelete();
}
