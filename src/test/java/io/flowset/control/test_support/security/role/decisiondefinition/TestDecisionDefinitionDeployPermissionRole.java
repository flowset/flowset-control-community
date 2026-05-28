/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.decisiondefinition;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Permission: decision definition deploy",
        code = TestDecisionDefinitionDeployPermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestDecisionDefinitionDeployPermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-decision-definition-deploy-permission";

    @SpecificPolicy(resources = SpecificPermissions.DECISION_DEFINITION_DEPLOY)
    void decisionDefinitionDeploy();
}
