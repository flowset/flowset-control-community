/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.variable;

import io.jmix.core.metamodel.datatype.EnumClass;

/**
 * Process variable type of an engine. Ids are the type names used by the engine API.
 */
public interface ProcessVariableType extends EnumClass<String> {

    /**
     * @return {@code true} if a value of this type is entered as a single field (string, number, boolean, date, null)
     */
    boolean isPrimitive();
}
