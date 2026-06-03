/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.processinstance;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to terminate a process instance.
 */
public class ProcessInstanceTerminateAccessContext extends SpecificOperationAccessContext {
    public ProcessInstanceTerminateAccessContext() {
        super(SpecificPermissions.PROCESS_INSTANCE_TERMINATE);
    }
}
