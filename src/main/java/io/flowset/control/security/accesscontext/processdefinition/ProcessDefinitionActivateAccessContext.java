/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.processdefinition;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to activate a process definition.
 */
public class ProcessDefinitionActivateAccessContext extends SpecificOperationAccessContext {
    public ProcessDefinitionActivateAccessContext() {
        super(SpecificPermissions.PROCESS_DEFINITION_ACTIVATE);
    }
}
