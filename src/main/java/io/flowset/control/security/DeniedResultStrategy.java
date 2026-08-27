/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.security;

/**
 * Enum representing strategies to handle situations where access to a secured resource is denied.
 * This is used in security-related configurations to define the behavior when a specific access
 * restriction is encountered.
 * <p/>
 * <ol>
 *     <li>TYPE_DEFAULT - Applies the default behavior for the secured resource.</li>
 *     <li>THROW_ACCESS_DENIED - Throws an exception to indicate access denial.</li>
 * </ol>
 */
public enum DeniedResultStrategy {
    TYPE_DEFAULT,
    THROW_ACCESS_DENIED
}
