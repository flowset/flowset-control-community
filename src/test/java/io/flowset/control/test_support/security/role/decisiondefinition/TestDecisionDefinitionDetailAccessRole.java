/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.decisiondefinition;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
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
        name = "Test Access: decision definition detail",
        code = TestDecisionDefinitionDetailAccessRole.CODE,
        scope = SecurityScope.UI
)
public interface TestDecisionDefinitionDetailAccessRole extends UiMinimalRole {

    String CODE = "test-decision-definition-detail-access";

    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    void uiAccess();

    @EntityPolicy(entityClass = DecisionDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionDefinitionData();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = DecisionInstanceFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionInstanceFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionInstanceFilter();
}
