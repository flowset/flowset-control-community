/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processinstance;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.test_support.security.role.main.TestMainMenuProcessInstanceRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: process instance list without process definition detail view",
        code = TestProcessInstanceListNoProcessDefinitionDetailViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessInstanceListNoProcessDefinitionDetailViewRole extends TestMainMenuProcessInstanceRole {

    String CODE = "test-process-instance-list-no-process-definition-detail-view";

    @EntityPolicy(entityClass = ProcessInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processInstanceData();

    @EntityPolicy(entityClass = ProcessInstanceFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessInstanceFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processInstanceFilter();

    @EntityPolicy(entityClass = ProcessDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionData();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();
}
