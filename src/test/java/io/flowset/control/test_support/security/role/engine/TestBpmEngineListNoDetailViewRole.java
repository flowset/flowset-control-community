/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.engine;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.test_support.security.role.main.TestMainMenuBpmEngineRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: BPM engine list without detail view",
        code = TestBpmEngineListNoDetailViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestBpmEngineListNoDetailViewRole extends TestMainMenuBpmEngineRole {

    String CODE = "test-bpm-engine-list-no-detail-view";

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();
}
