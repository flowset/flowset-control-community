/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.exception;

import io.jmix.core.EntityAccessException;
import io.jmix.core.metamodel.model.MetaClass;
import lombok.Getter;

@Getter
public class EngineEntityAccessException extends EntityAccessException {
    protected MetaClass metaClass;
    protected Object entityId;

    public EngineEntityAccessException(MetaClass metaClass, Object entityId) {
        super(metaClass, entityId);
        this.metaClass = metaClass;
        this.entityId = entityId;
    }
}
