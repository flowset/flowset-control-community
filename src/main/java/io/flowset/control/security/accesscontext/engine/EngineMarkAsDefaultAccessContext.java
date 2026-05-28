/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security.accesscontext.engine;

import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Grants permission to mark an engine as default.
 */
public class EngineMarkAsDefaultAccessContext extends SpecificOperationAccessContext {
    public EngineMarkAsDefaultAccessContext() {
        super(SpecificPermissions.ENGINE_MARK_AS_DEFAULT);
    }
}
