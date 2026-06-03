/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processdefinition;

import io.flowset.control.entity.activity.ProcessActivityStatistics;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisiondefinition.DecisionReferenceData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processdefinition.CalledProcessReferenceData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import io.flowset.control.test_support.security.role.main.TestMainMenuProcessDefinitionRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(
        name = "Test Access: process definition detail without decision detail view",
        code = TestProcessDefinitionDetailNoDecisionDetailViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessDefinitionDetailNoDecisionDetailViewRole extends TestMainMenuProcessDefinitionRole {

    String CODE = "test-process-definition-detail-no-decision-detail-view";

    @ViewPolicy(viewIds = "bpm_ProcessDefinition.detail")
    void processDefinitionDetailView();

    @EntityPolicy(entityClass = ProcessDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processDefinitionData();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = RuntimeProcessInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = RuntimeProcessInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void runtimeProcessInstanceData();

    @EntityPolicy(entityClass = ProcessInstanceFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessInstanceFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processInstanceFilter();

    @EntityPolicy(entityClass = CalledProcessReferenceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = CalledProcessReferenceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void calledProcessReferenceData();

    @EntityPolicy(entityClass = DecisionReferenceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionReferenceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionReferenceData();

    @EntityPolicy(entityClass = DecisionDefinitionData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionDefinitionData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionDefinitionData();

    @EntityPolicy(entityClass = ProcessActivityStatistics.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessActivityStatistics.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processActivityStatistics();
}
