/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.decisiondefinition;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.test_support.security.role.main.TestMainMenuDecisionDefinitionRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: decision definition list without detail view",
        code = TestDecisionDefinitionListNoDetailViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestDecisionDefinitionListNoDetailViewRole extends TestMainMenuDecisionDefinitionRole {

    String CODE = "test-decision-definition-list-no-detail-view";

    @EntityPolicy(entityClass = DecisionDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionDefinitionData();

    @EntityPolicy(entityClass = DecisionDefinitionFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionDefinitionFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionDefinitionFilter();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();
}
