/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security;

import io.flowset.control.test_support.ControlTestDataCreator;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

/**
 * JUnit extension that creates a test user declared in {@link WithTestUser} before the test and removes it after.
 */
public class WithTestUserExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        WithTestUser annotation = getAnnotation(context);
        removeUser(context, annotation.username());
        getControlTestDataCreator(context).createUser(annotation.username(), annotation.password(), annotation.roles());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        WithTestUser annotation = getAnnotation(context);
        removeUser(context, annotation.username());
    }

    private WithTestUser getAnnotation(ExtensionContext context) {
        Optional<WithTestUser> methodAnnotation = context.getTestMethod()
                .flatMap(method -> AnnotationSupport.findAnnotation(method, WithTestUser.class));
        if (methodAnnotation.isPresent()) {
            return methodAnnotation.get();
        }

        Optional<WithTestUser> classAnnotation = context.getTestClass()
                .flatMap(testClass -> AnnotationSupport.findAnnotation(testClass, WithTestUser.class));
        if (classAnnotation.isPresent()) {
            return classAnnotation.get();
        }

        throw new ExtensionConfigurationException(
                "@" + WithTestUser.class.getSimpleName() + " is required for " + context.getDisplayName());
    }

    /**
     * Removes the user from the database.
     *
     * @param context  JUnit extension context
     * @param username username of the test user to remove
     */
    private void removeUser(ExtensionContext context, String username) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(context);
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME = ?", username);
        jdbcTemplate.update("delete from USER_ where USERNAME = ?", username);
    }

    private ControlTestDataCreator getControlTestDataCreator(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        return applicationContext.getBean(ControlTestDataCreator.class);
    }

    private JdbcTemplate getJdbcTemplate(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        return applicationContext.getBean(JdbcTemplate.class);
    }
}
