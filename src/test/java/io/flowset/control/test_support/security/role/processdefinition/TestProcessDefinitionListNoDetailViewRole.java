/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processdefinition;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.test_support.security.role.main.TestMainMenuProcessDefinitionRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: process definition list without detail view",
        code = TestProcessDefinitionListNoDetailViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessDefinitionListNoDetailViewRole extends TestMainMenuProcessDefinitionRole {

    String CODE = "test-process-definition-list-no-detail-view";

    @EntityPolicy(entityClass = ProcessDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionData();

    @EntityPolicy(entityClass = ProcessDefinitionFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionFilter();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();
}
