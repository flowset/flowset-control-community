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
        name = "Test User: change password view",
        code = TestUserChangePasswordViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestUserChangePasswordViewRole extends TestUserListAccessRole {

    String CODE = "test-user-change-password-view";

    @ViewPolicy(viewIds = "changePasswordView")
    void changePasswordView();

    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.UPDATE)
    void userEdit();
}
