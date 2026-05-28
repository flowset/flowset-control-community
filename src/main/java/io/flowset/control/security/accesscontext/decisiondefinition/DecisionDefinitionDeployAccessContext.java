/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.decisiondefinition;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to deploy a decision definition.
 */
public class DecisionDefinitionDeployAccessContext extends SpecificOperationAccessContext {

    public DecisionDefinitionDeployAccessContext() {
        super(SpecificPermissions.DECISION_DEFINITION_DEPLOY);
    }
}
