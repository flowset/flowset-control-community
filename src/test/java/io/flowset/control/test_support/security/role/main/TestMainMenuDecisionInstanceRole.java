/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.main;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Test Main menu: decision instances item",
        code = TestMainMenuDecisionInstanceRole.CODE,
        scope = SecurityScope.UI)
public interface TestMainMenuDecisionInstanceRole extends TestMainMenuBaseRole {

    String CODE = "test-main-menu-decision-instances";

    @ViewPolicy(viewIds = "DecisionInstanceData.list")
    @MenuPolicy(menuIds = "decisionInstances")
    void decisionInstances();
}
