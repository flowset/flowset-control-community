/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processinstance;

import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.activity.ActivityInstanceTreeItem;
import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.entity.filter.ExternalTaskFilter;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.filter.UserTaskFilter;
import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.entity.variable.VariableInstanceData;
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
        name = "Test Access: process instance list without process definition view",
        code = TestProcessInstanceNoProcessDefinitionViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessInstanceNoProcessDefinitionViewRole extends UiMinimalRole {

    String CODE = "test-process-instance-no-process-definition-view";

    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    void uiAccess();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = ProcessInstanceFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessInstanceFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processInstanceFilter();

    @EntityPolicy(entityClass = ProcessInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ProcessInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void processInstanceData();

    @EntityPolicy(entityClass = RuntimeProcessInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = RuntimeProcessInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void runtimeProcessInstanceData();

    @EntityPolicy(entityClass = ActivityInstanceTreeItem.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ActivityInstanceTreeItem.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void activityInstanceTreeItem();

    @EntityPolicy(entityClass = VariableInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = VariableInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void variableInstanceData();

    @EntityPolicy(entityClass = UserTaskData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = UserTaskData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void userTaskData();

    @EntityPolicy(entityClass = JobData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = JobData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void jobData();

    @EntityPolicy(entityClass = ExternalTaskData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ExternalTaskData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void externalTaskData();

    @EntityPolicy(entityClass = IncidentData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = IncidentData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void incidentData();

    @EntityPolicy(entityClass = HistoricActivityInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = HistoricActivityInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void historicActivityInstanceData();

    @EntityPolicy(entityClass = HistoricIncidentData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = HistoricIncidentData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void historicIncidentData();

    @EntityPolicy(entityClass = HistoricVariableInstanceData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = HistoricVariableInstanceData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void historicVariableInstanceData();

    @EntityPolicy(entityClass = VariableFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = VariableFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void variableFilter();

    @EntityPolicy(entityClass = UserTaskFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = UserTaskFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void userTaskFilter();

    @EntityPolicy(entityClass = JobFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = JobFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void jobFilter();

    @EntityPolicy(entityClass = ExternalTaskFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = ExternalTaskFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void externalTaskFilter();

    @EntityPolicy(entityClass = IncidentFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = IncidentFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void incidentFilter();

    @EntityPolicy(entityClass = DecisionInstanceFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DecisionInstanceFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void decisionInstanceFilter();
}
