/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.main;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;

@ResourceRole(name = "Test Main menu: dashboard item", code = TestMainMenuDashboardRole.CODE, scope = SecurityScope.UI)
public interface TestMainMenuDashboardRole extends TestMainMenuBaseRole {

    String CODE = "test-main-menu-dashboard";

    @MenuPolicy(menuIds = "dashboard")
    void dashboard();
}
