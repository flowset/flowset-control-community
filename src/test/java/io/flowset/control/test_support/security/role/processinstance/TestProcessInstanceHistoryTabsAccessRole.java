/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processinstance;

import io.flowset.control.entity.decisioninstance.HistoricDecisionInputInstanceShortData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionOutputInstanceShortData;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(
        name = "Test Access: process instance history tabs with full entity read",
        code = TestProcessInstanceHistoryTabsAccessRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessInstanceHistoryTabsAccessRole extends TestProcessInstanceDetailNoLinkedDetailViewRole {

    String CODE = "test-process-instance-history-tabs";

    @ViewPolicy(viewIds = {
            "HistoricActivityInstanceData.detail",
            "HistoricIncidentData.detail",
            "bpm_UserTaskData.detail",
            "HistoricVariableInstanceData.detail",
            "bpm_DecisionInstance.detail"
    })
    void linkedDetailViews();
}
