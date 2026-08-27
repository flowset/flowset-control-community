/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Test UI: permission visibility base",
        code = TestUiPermissionFullAccessReadRole.CODE,
        scope = SecurityScope.UI)
public interface TestUiPermissionFullAccessReadRole {

    String CODE = "test-ui-permission-visibility-base";

    @EntityPolicy(entityName = "*", actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityName = "*", attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    @SpecificPolicy(resources = "ui.loginToUi")
    void fullReadAccess();
}
