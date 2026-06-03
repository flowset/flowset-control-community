/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.usertask;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to complete a user task.
 */
public class UserTaskCompleteAccessContext extends SpecificOperationAccessContext {
    public UserTaskCompleteAccessContext() {
        super(SpecificPermissions.USER_TASK_COMPLETE);
    }
}
