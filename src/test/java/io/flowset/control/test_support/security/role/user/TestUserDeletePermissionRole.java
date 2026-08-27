/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.user;

import io.flowset.control.entity.User;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test User: delete permission",
        code = TestUserDeletePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestUserDeletePermissionRole extends TestUserListAccessRole {

    String CODE = "test-user-delete-permission";

    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.DELETE)
    void userDelete();
}
