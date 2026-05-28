/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.batch;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.security.UiMinimalRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(
        name = "Test Access: batch list",
        code = TestBatchListAccessRole.CODE,
        scope = SecurityScope.UI
)
public interface TestBatchListAccessRole extends UiMinimalRole {

    String CODE = "test-batch-list-access";

    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    void uiAccess();

    @EntityPolicy(entityClass = BpmEngine.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BpmEngine.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void bpmEngine();

    @EntityPolicy(entityClass = BatchFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BatchFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void batchFilter();

    @EntityPolicy(entityClass = BatchStatisticsData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BatchStatisticsData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void batchStatisticsData();

    @EntityPolicy(entityClass = BatchData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BatchData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void batchData();
}
