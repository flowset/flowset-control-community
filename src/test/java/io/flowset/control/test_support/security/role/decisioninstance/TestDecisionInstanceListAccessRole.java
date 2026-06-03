/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.decisioninstance;

import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
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
        name = "Test Access: decision instance list",
        code = TestDecisionInstanceListAccessRole.CODE,
        scope = SecurityScope.UI
)
public interface TestDecisionInstanceListAccessRole extends UiMinimalRole {

    String CODE = "test-decision-instance-list-access";

    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    void uiAccess();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = DecisionInstanceFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionInstanceFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionInstanceFilter();

    @EntityPolicy(entityClass = HistoricDecisionInstanceShortData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = HistoricDecisionInstanceShortData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void historicDecisionInstanceShortData();
}
