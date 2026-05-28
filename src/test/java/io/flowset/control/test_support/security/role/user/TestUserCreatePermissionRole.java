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
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(
        name = "Test User: create permission",
        code = TestUserCreatePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestUserCreatePermissionRole extends TestUserListAccessRole {

    String CODE = "test-user-create-permission";

    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.CREATE)
    @ViewPolicy(viewIds = "User.detail")
    void userCreate();
}
