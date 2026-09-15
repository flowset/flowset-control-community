/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security;

import io.flowset.control.security.FullAccessRole;
import io.flowset.control.security.UiMinimalRole;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.jmix.security.role.annotation.ResourceRole;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * JUnit extension that creates a test user declared in {@link WithTestUser} before the test and removes it after.
 */
public class WithTestUserExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        WithTestUser annotation = getAnnotation(context);
        removeTestData(context, annotation);
        getControlTestDataCreator(context).createUser(annotation.username(), annotation.password(), annotation.roles());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        removeTestData(context, getAnnotation(context));
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
     * Removes the test user and the roles created for it from the database.
     *
     * @param context    JUnit extension context
     * @param annotation annotation declaring the test user
     */
    private void removeTestData(ExtensionContext context, WithTestUser annotation) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(context);

        removeUser(jdbcTemplate, annotation.username());
        removeDatabaseRoles(jdbcTemplate, annotation.roles());
    }

    /**
     * Removes the user from the database.
     *
     * @param jdbcTemplate JDBC template of the test application context
     * @param username     username of the test user to remove
     */
    private void removeUser(JdbcTemplate jdbcTemplate, String username) {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT where USERNAME = ?", username);
        jdbcTemplate.update("delete from USER_ where USERNAME = ?", username);
    }

    /**
     * Removes runtime copies of the test roles saved by
     * {@link ControlTestDataCreator#createUser(String, String, Class[])}. They share codes with the design-time roles,
     * so leaving them in the database makes every application context created later fail to start with
     * {@code DuplicateRoleCodeException}.
     *
     * @param jdbcTemplate JDBC template of the test application context
     * @param roleClasses  resource role classes assigned to the test user
     */
    private void removeDatabaseRoles(JdbcTemplate jdbcTemplate, Class<?>[] roleClasses) {
        List<String> roleCodes = Stream.of(roleClasses)
                .filter(this::isTestRole)
                .map(roleClass -> roleClass.getAnnotation(ResourceRole.class))
                .filter(Objects::nonNull)
                .map(ResourceRole::code)
                .toList();

        if (roleCodes.isEmpty()) {
            return;
        }

        String codeParams = String.join(", ", Collections.nCopies(roleCodes.size(), "?"));
        Object[] codes = roleCodes.toArray();

        jdbcTemplate.update("delete from SEC_RESOURCE_POLICY where ROLE_ID in "
                + "(select ID from SEC_RESOURCE_ROLE where CODE in (" + codeParams + "))", codes);
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE where CODE in (" + codeParams + ")", codes);
    }

    /**
     * Roles defined in the application are always available as design-time roles, so
     * {@link ControlTestDataCreator} does not save them to the database and they must never be removed from it.
     *
     * @param roleClass resource role class
     * @return true if the role is a test role saved to the database
     */
    private boolean isTestRole(Class<?> roleClass) {
        return !roleClass.equals(UiMinimalRole.class) && !roleClass.equals(FullAccessRole.class);
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
