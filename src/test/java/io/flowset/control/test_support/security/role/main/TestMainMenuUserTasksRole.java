/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.main;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Test Main menu: user tasks item", code = TestMainMenuUserTasksRole.CODE, scope = SecurityScope.UI)
public interface TestMainMenuUserTasksRole extends TestMainMenuBaseRole {

    String CODE = "test-main-menu-user-tasks";

    @ViewPolicy(viewIds = "bpm_AllTasksView")
    @MenuPolicy(menuIds = "userTasks")
    void userTasks();
}
