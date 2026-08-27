/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

/**
 * Checks whether a test annotated with {@link EnabledOnNoAuthentication} should run for the
 * external engine configured in {@link ControlUiTestingProperties#getEngine()}.
 */
public class NoAuthenticationCondition implements ExecutionCondition {

    /**
     * Evaluates whether to run the current test based on {@link EnabledOnNoAuthentication}.
     * The condition only applies for the {@code ui-test} profile.
     * The test is enabled if:
     * <ol>
     *     <li>The element is not annotated with {@link EnabledOnNoAuthentication}</li>
     *     <li>The {@code ui-test} profile is not active, so the rule is not applied</li>
     *     <li>The configured external engine exists and has no authentication configured</li>
     * </ol>
     *
     * @param context the current extension context; never {@code null}
     * @return the evaluation result describing whether the test should run
     */
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<EnabledOnNoAuthentication> enabledOnNoAuthentication =
                findAnnotation(context.getElement(), EnabledOnNoAuthentication.class);
        if (enabledOnNoAuthentication.isPresent()) {
            return evaluateEnabledOnNoAuthentication(context);
        }

        return enabledByDefault();
    }

    protected ConditionEvaluationResult evaluateEnabledOnNoAuthentication(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        Environment environment = applicationContext.getEnvironment();

        if (environment.matchesProfiles(Constants.UI_TEST_PROFILE)) {
            return evaluateForExternalEngine(applicationContext);
        }
        return enabled("UI test profile is not used");
    }

    private ConditionEvaluationResult evaluateForExternalEngine(ApplicationContext applicationContext) {
        ExternalEngine engine = applicationContext.getBean(ControlUiTestingProperties.class).getEngine();
        if (engine == null) {
            return disabled("External engine is not configured");
        }
        return engine.getAuthType() == null
                ? enabled("External engine '" + engine.getName() + "' uses no authentication")
                : disabled("External engine '" + engine.getName() + "' uses authentication " +
                        "(authType=" + engine.getAuthType() + ")");
    }

    private ConditionEvaluationResult enabledByDefault() {
        return enabled("No-authentication rule is not present");
    }
}
