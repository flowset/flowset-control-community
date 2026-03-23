/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.exception;

/**
 * Thrown if a connection error occurs during the access token exchange.
 */
public class AccessTokenConnectException extends RuntimeException {
    public AccessTokenConnectException(String message) {
        super(message);
    }

    public AccessTokenConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
