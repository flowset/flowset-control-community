/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.engine;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.test_support.security.TestUiPermissionFullAccessReadRole;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Permission: BPM engine delete",
        code = TestBpmEngineDeletePermissionRole.CODE,
        scope = SecurityScope.UI
)
public interface TestBpmEngineDeletePermissionRole extends TestUiPermissionFullAccessReadRole {

    String CODE = "test-bpm-engine-delete-permission";

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.DELETE)
    void bpmEngineDelete();
}
