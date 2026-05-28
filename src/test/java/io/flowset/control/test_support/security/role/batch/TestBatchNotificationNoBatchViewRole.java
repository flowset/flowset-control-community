/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security.role.batch;

import io.flowset.control.security.SpecificPermissions;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceNoProcessDefinitionViewRole;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(
        name = "Test Access: batch notification without batch view",
        code = TestBatchNotificationNoBatchViewRole.CODE,
        scope = SecurityScope.UI
)
public interface TestBatchNotificationNoBatchViewRole extends TestProcessInstanceNoProcessDefinitionViewRole {

    String CODE = "test-batch-notification-no-batch-view";

    @SpecificPolicy(resources = SpecificPermissions.PROCESS_INSTANCE_TERMINATE)
    void processInstanceTerminate();
}
