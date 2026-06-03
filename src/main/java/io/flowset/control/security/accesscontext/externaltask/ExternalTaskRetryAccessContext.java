/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.externaltask;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to retry an external task.
 */
public class ExternalTaskRetryAccessContext extends SpecificOperationAccessContext {
    public ExternalTaskRetryAccessContext() {
        super(SpecificPermissions.EXTERNAL_TASK_RETRY);
    }
}
