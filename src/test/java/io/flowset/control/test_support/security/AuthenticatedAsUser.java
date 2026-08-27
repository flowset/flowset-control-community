/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables system authentication with the specified user for an integration test.
 * Can be declared on a test class or on an individual test method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Order(Integer.MAX_VALUE)
@ExtendWith(AuthenticatedAsUserExtension.class)
@Inherited
public @interface AuthenticatedAsUser {

    /**
     *
     * @return username of the user to authenticate with
     */
    String username();
}
