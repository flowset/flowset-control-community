/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.processdefinition;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Permission: process definition deploy",
        code = TestProcessDefinitionDeployPermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestProcessDefinitionDeployPermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-process-definition-deploy-permission";

    @SpecificPolicy(resources = SpecificPermissions.PROCESS_DEFINITION_DEPLOY)
    void processDefinitionDeploy();
}
