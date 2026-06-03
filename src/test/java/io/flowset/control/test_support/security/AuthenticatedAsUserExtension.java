/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.security;

import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * JUnit extension that authenticates a test as the user declared in {@link AuthenticatedAsUser}.
 */
public class AuthenticatedAsUserExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        getSystemAuthenticator(context).begin(getUsername(context));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        getSystemAuthenticator(context).end();
    }

    private String getUsername(ExtensionContext context) {
        Optional<AuthenticatedAsUser> methodAnnotation = context.getTestMethod()
                .flatMap(method -> AnnotationSupport.findAnnotation(method, AuthenticatedAsUser.class));
        if (methodAnnotation.isPresent()) {
            return validateUsername(methodAnnotation.get().username(), context);
        }

        Optional<AuthenticatedAsUser> classAnnotation = context.getTestClass()
                .flatMap(testClass -> AnnotationSupport.findAnnotation(testClass, AuthenticatedAsUser.class));
        if (classAnnotation.isPresent()) {
            return validateUsername(classAnnotation.get().username(), context);
        }

        throw new ExtensionConfigurationException(
                "@" + AuthenticatedAsUser.class.getSimpleName() + " is required for " + context.getDisplayName());
    }

    private String validateUsername(String username, ExtensionContext context) {
        if (!StringUtils.hasText(username)) {
            throw new ExtensionConfigurationException(
                    "Username in @" + AuthenticatedAsUser.class.getSimpleName()
                            + " must not be blank for " + context.getDisplayName());
        }
        return username;
    }

    private SystemAuthenticator getSystemAuthenticator(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        return applicationContext.getBean(SystemAuthenticator.class);
    }
}
