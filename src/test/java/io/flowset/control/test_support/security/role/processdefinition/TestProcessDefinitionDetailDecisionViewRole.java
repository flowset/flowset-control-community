/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processdefinition;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: process definition detail with decision view",
        code = TestProcessDefinitionDetailDecisionViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessDefinitionDetailDecisionViewRole extends TestProcessDefinitionDetailAccessRole {

    String CODE = "test-process-definition-detail-decision-view";

    @EntityPolicy(entityClass = DecisionDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionDefinitionData();
}
