/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.main;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Test Main menu: incidents item", code = TestMainMenuIncidentsRole.CODE, scope = SecurityScope.UI)
public interface TestMainMenuIncidentsRole extends TestMainMenuBaseRole {

    String CODE = "test-main-menu-incidents";

    @ViewPolicy(viewIds = "IncidentData.list")
    @MenuPolicy(menuIds = "incidents")
    void incidents();
}
