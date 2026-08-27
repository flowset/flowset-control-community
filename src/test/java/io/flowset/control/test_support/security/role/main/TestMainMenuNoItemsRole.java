/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.main;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Test Main menu: no items", code = TestMainMenuNoItemsRole.CODE, scope = SecurityScope.UI)
public interface TestMainMenuNoItemsRole extends TestMainMenuBaseRole {

    String CODE = "test-main-menu-no-items";
}
