/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.decisiondefinition;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.test_support.security.role.main.TestMainMenuDecisionDefinitionRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(
        name = "Test Access: decision definition detail without linked detail views",
        code = TestDecisionDefinitionDetailNoLinkedDetailViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestDecisionDefinitionDetailNoLinkedDetailViewRole extends TestMainMenuDecisionDefinitionRole {

    String CODE = "test-decision-definition-detail-no-linked-detail-view";

    @ViewPolicy(viewIds = "bpm_DecisionDefinition.detail")
    void decisionDefinitionDetailView();

    @EntityPolicy(entityClass = DecisionDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionDefinitionData();

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

    @EntityPolicy(entityClass = ProcessInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processInstanceData();

    @EntityPolicy(entityClass = ProcessDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionData();
}
