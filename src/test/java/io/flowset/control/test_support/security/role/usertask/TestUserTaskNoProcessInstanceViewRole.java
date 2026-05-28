/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.usertask;

import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.UserTaskFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.UiMinimalRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(
        name = "Test Access: user task without process instance view",
        code = TestUserTaskNoProcessInstanceViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestUserTaskNoProcessInstanceViewRole extends UiMinimalRole {

    String CODE = "test-user-task-no-process-instance-view";

    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    void uiAccess();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = UserTaskFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = UserTaskFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void userTaskFilter();

    @EntityPolicy(entityClass = UserTaskData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = UserTaskData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void userTaskData();

    @EntityPolicy(entityClass = ProcessDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionData();
}
