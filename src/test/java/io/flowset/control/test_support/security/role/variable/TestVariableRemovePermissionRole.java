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
        name = "Test Permission: variable remove",
        code = TestVariableRemovePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestVariableRemovePermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-variable-remove-permission";

    @EntityPolicy(entityClass = VariableInstanceData.class, actions = EntityPolicyAction.DELETE)
    void variableRemove();
}
