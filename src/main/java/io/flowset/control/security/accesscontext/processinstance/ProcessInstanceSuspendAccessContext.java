/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.processinstance;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to suspend a process instance.
 */
public class ProcessInstanceSuspendAccessContext extends SpecificOperationAccessContext {
    public ProcessInstanceSuspendAccessContext() {
        super(SpecificPermissions.PROCESS_INSTANCE_SUSPEND);
    }
}
