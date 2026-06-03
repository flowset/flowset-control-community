/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.deployment;

import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.test_support.security.role.main.TestMainMenuDeploymentsRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: deployment list without detail view",
        code = TestDeploymentListNoDetailViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestDeploymentListNoDetailViewRole extends TestMainMenuDeploymentsRole {

    String CODE = "test-deployment-list-no-detail-view";

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = DeploymentData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DeploymentData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void deploymentData();

    @EntityPolicy(entityClass = DeploymentFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DeploymentFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void deploymentFilter();
}
