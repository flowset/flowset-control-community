/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.externaltask;

import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.test_support.security.role.incident.TestIncidentNoProcessDefinitionViewRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: external task detail without process definition view",
        code = TestExternalTaskNoProcessDefinitionViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestExternalTaskNoProcessDefinitionViewRole extends TestIncidentNoProcessDefinitionViewRole {

    String CODE = "test-external-task-no-process-definition-view";

    @EntityPolicy(entityClass = ExternalTaskData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ExternalTaskData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void externalTaskData();
}
