/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.incident;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to retry an incident.
 */
public class IncidentRetryAccessContext extends SpecificOperationAccessContext {
    public IncidentRetryAccessContext() {
        super(SpecificPermissions.INCIDENT_RETRY);
    }
}
