/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.batch;

import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Test Access: batch detail", code = TestBatchDetailAccessRole.CODE, scope = SecurityScope.UI)
public interface TestBatchDetailAccessRole extends TestBatchListAccessRole {

    String CODE = "test-batch-detail-access";

    @EntityPolicy(entityClass = JobFilter.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = JobFilter.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void jobFilter();

    @EntityPolicy(entityClass = JobData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = JobData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void jobData();
}
