/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.user;

import io.flowset.control.entity.User;
import io.flowset.control.test_support.security.role.main.TestMainMenuUserRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test User: list access",
        code = TestUserListAccessRole.CODE,
        scope = SecurityScope.UI
)
public interface TestUserListAccessRole extends TestMainMenuUserRole {

    String CODE = "test-user-list-access";

    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void userRead();
}
