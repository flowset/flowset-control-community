/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processdefinition;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.security.UiMinimalRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Grants navigation access to the process list view but no view permission on
 * {@link io.flowset.control.entity.processdefinition.ProcessDefinitionData}, so that
 * actions gated on that permission are hidden.
 */
@ResourceRole(
        name = "Test Access: no process definition data view",
        code = TestProcessDefinitionNoDataViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessDefinitionNoDataViewRole extends UiMinimalRole {

    String CODE = "test-no-process-definition-data-view";

    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    void uiAccess();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = ProcessDefinitionFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionFilter();
}
