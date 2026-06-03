/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.incident;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.IncidentData;
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
        name = "Test Access: incident without process instance view",
        code = TestIncidentNoProcessInstanceViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestIncidentNoProcessInstanceViewRole extends UiMinimalRole {

    String CODE = "test-incident-no-process-instance-view";

    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    void uiAccess();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = IncidentFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = IncidentFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void incidentFilter();

    @EntityPolicy(entityClass = IncidentData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = IncidentData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void incidentData();

    @EntityPolicy(entityClass = ProcessDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionData();
}
