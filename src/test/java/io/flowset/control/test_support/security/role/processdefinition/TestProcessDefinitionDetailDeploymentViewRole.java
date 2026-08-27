/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processdefinition;

import io.flowset.control.entity.deployment.DeploymentData;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: process definition detail with deployment view",
        code = TestProcessDefinitionDetailDeploymentViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessDefinitionDetailDeploymentViewRole extends TestProcessDefinitionDetailAccessRole {

    String CODE = "test-process-definition-detail-deployment-view";

    @EntityPolicy(entityClass = DeploymentData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = DeploymentData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void deploymentData();
}
