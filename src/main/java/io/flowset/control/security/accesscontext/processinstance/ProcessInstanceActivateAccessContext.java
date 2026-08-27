/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.processinstance;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to activate a process instance.
 */
public class ProcessInstanceActivateAccessContext extends SpecificOperationAccessContext {
    public ProcessInstanceActivateAccessContext() {
        super(SpecificPermissions.PROCESS_INSTANCE_ACTIVATE);
    }
}
