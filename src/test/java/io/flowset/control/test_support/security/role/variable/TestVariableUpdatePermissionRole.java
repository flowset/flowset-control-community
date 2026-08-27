/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.variable;

import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Permission: variable update",
        code = TestVariableUpdatePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestVariableUpdatePermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-variable-update-permission";

    @EntityPolicy(entityClass = VariableInstanceData.class, actions = EntityPolicyAction.UPDATE)
    void variableUpdate();
}
