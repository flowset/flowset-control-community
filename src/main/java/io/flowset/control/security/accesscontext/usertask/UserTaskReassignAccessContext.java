/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.usertask;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to reassign a user task.
 */
public class UserTaskReassignAccessContext extends SpecificOperationAccessContext {
    public UserTaskReassignAccessContext() {
        super(SpecificPermissions.USER_TASK_REASSIGN);
    }
}
