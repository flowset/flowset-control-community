/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.processdefinition;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to deploy a process definition.
 */
public class ProcessDefinitionDeployAccessContext extends SpecificOperationAccessContext {

    public ProcessDefinitionDeployAccessContext() {
        super(SpecificPermissions.PROCESS_DEFINITION_DEPLOY);
    }
}
