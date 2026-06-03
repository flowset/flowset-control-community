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
        name = "Test Permission: variable create",
        code = TestVariableCreatePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestVariableCreatePermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-variable-create-permission";

    @EntityPolicy(entityClass = VariableInstanceData.class, actions = EntityPolicyAction.CREATE)
    void variableCreate();
}
