/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.main;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Test Main menu: process definitions item",
        code = TestMainMenuProcessDefinitionRole.CODE,
        scope = SecurityScope.UI)
public interface TestMainMenuProcessDefinitionRole extends TestMainMenuBaseRole {

    String CODE = "test-main-menu-process-definitions";

    @ViewPolicy(viewIds = "bpm_ProcessDefinition.list")
    @MenuPolicy(menuIds = "processDefinitions")
    void processDefinitions();
}
