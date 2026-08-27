/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Creates a test user with the specified roles before a test and removes the user after the test.
 * Can be declared on a test class or on a test method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Order(Integer.MAX_VALUE - 1)
@ExtendWith(WithTestUserExtension.class)
@Inherited
public @interface WithTestUser {

    /**
     * @return user of the test user
     */
    String username();

    /**
     * @return password of the test user
     */
    String password() default "password";

    /**
     * @return roles assigned to the test user
     */
    Class<?>[] roles() default {};
}
