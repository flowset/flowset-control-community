/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.job;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to retry a job.
 */
public class JobRetryAccessContext extends SpecificOperationAccessContext {
    public JobRetryAccessContext() {
        super(SpecificPermissions.JOB_RETRY);
    }
}
