/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.processdefinition;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to migrate a process definition.
 */
public class ProcessDefinitionMigrateAccessContext extends SpecificOperationAccessContext {
    public ProcessDefinitionMigrateAccessContext() {
        super(SpecificPermissions.PROCESS_DEFINITION_MIGRATE);
    }
}
