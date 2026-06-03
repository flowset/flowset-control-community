/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.user;

import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(
        name = "Test User: substitutions view",
        code = TestUserSubstitutionViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestUserSubstitutionViewRole extends TestUserListAccessRole {

    String CODE = "test-user-substitution-view";

    @ViewPolicy(viewIds = "sec_UserSubstitution.view")
    void userSubstitutionView();

    @EntityPolicy(entityClass = UserSubstitutionEntity.class, actions = EntityPolicyAction.READ)
    void userSubstitutionRead();
}
