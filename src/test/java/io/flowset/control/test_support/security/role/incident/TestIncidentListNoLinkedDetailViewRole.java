/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.incident;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.test_support.security.role.main.TestMainMenuIncidentsRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: incident list without linked detail views",
        code = TestIncidentListNoLinkedDetailViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestIncidentListNoLinkedDetailViewRole extends TestMainMenuIncidentsRole {

    String CODE = "test-incident-list-no-linked-detail-view";

    @EntityPolicy(entityClass = IncidentData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = IncidentData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void incidentData();

    @EntityPolicy(entityClass = IncidentFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = IncidentFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void incidentFilter();

    @EntityPolicy(entityClass = ProcessInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processInstanceData();

    @EntityPolicy(entityClass = ProcessDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionData();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();
}
