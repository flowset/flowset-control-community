/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.job;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to activate a job.
 */
public class JobActivateAccessContext extends SpecificOperationAccessContext {
    public JobActivateAccessContext() {
        super(SpecificPermissions.JOB_ACTIVATE);
    }
}
