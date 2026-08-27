/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.main;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Test Main menu: BPM engines administration item",
        code = TestMainMenuBpmEngineRole.CODE,
        scope = SecurityScope.UI)
public interface TestMainMenuBpmEngineRole extends TestMainMenuBaseRole {

    String CODE = "test-main-menu-bpm-engine";

    @ViewPolicy(viewIds = "BpmEngine.list")
    @MenuPolicy(menuIds = "BpmEngine.list")
    void bpmEngines();
}
