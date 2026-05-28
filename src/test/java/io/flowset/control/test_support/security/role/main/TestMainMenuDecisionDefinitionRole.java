/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.main;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Test Main menu: decisions item",
        code = TestMainMenuDecisionDefinitionRole.CODE,
        scope = SecurityScope.UI)
public interface TestMainMenuDecisionDefinitionRole extends TestMainMenuBaseRole {

    String CODE = "test-main-menu-decision-definitions";

    @ViewPolicy(viewIds = "bpm_DecisionDefinition.list")
    @MenuPolicy(menuIds = "decisions")
    void decisions();
}
