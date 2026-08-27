/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.decisiondefinition;

import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: decision definition detail with decision instance view",
        code = TestDecisionDetailDecisionInstanceViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestDecisionDetailDecisionInstanceViewRole extends TestDecisionDefinitionDetailAccessRole {

    String CODE = "test-decision-definition-detail-decision-instance-view";

    @EntityPolicy(entityClass = HistoricDecisionInstanceShortData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = HistoricDecisionInstanceShortData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionInstanceData();
}
