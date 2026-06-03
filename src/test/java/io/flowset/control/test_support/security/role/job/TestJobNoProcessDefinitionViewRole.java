/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.job;

import io.flowset.control.entity.job.JobData;
import io.flowset.control.test_support.security.role.incident.TestIncidentNoProcessDefinitionViewRole;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(
        name = "Test Access: job detail without process definition view",
        code = TestJobNoProcessDefinitionViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestJobNoProcessDefinitionViewRole extends TestIncidentNoProcessDefinitionViewRole {

    String CODE = "test-job-no-process-definition-view";

    @EntityPolicy(entityClass = JobData.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = JobData.class, attributes = "*",
            action = EntityAttributePolicyAction.VIEW)
    void jobData();
}
