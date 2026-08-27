/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.batch;

import io.flowset.control.entity.batch.BatchData;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: batch notification with batch view",
        code = TestBatchNotificationViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestBatchNotificationViewRole extends TestBatchNotificationNoBatchViewRole {

    String CODE = "test-batch-notification-view";

    @EntityPolicy(entityClass = BatchData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = BatchData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void batchData();
}
