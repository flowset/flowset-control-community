/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.test_support.property.ControlEngineTestingProperties;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import org.apache.commons.lang3.StringUtils;
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
 * Checks whether to run the test for the engine authentication type set in {@link ControlEngineTestingProperties#getAuthType()}
 * and in {@link ControlUiTestingProperties#getEngine().getAuthType()}.
 */
public class BasicAuthenticationCondition implements ExecutionCondition {

    /**
     * Evaluates whether to run the test based on the engine authentication type.
     * The test is run if:
     * <ol>
     *     <li>The test is not annotated with {@link EnabledOnBasicAuthentication}</li>
     *     <li>Spring profile <code>test-engine</code> is not used when running tests (not used by default)</li>
     *     <li>The test is not annotated with {@link EnabledOnBasicAuthentication} and {@link ControlEngineTestingProperties#getAuthType()} is Basic</li>
     * </ol>
     *
     * @param context the current extension context; never {@code null}
     * @return is testing allowed for a BPM engine with basic authenticated
     */
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<EnabledOnBasicAuthentication> enabledOnBasicAuthentication = findAnnotation(context.getElement(), EnabledOnBasicAuthentication.class);
        if (enabledOnBasicAuthentication.isPresent()) {
            return evaluateEnabledOnBasicAuthentication(context);
        }

        return enabledByDefault();
    }

    protected ConditionEvaluationResult evaluateEnabledOnBasicAuthentication(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        Environment environment = applicationContext.getEnvironment();

        if (environment.matchesProfiles(Constants.UI_TEST_PROFILE)) {
            return evaluateForExternalEngine(applicationContext);
        }

        boolean testEngineProfileUsed = environment.matchesProfiles(Constants.TEST_ENGINE_PROFILE);
        if (!testEngineProfileUsed) {
            return enabled("Test engine profile is not used");
        }

        ControlEngineTestingProperties testingProperties = applicationContext.getBean(ControlEngineTestingProperties.class);
        String authTypeProperty = testingProperties.getAuthType();
        if (StringUtils.isEmpty(authTypeProperty)) {
            return disabled("Authentication type is not set");
        }

        AuthType authType = AuthType.fromId(authTypeProperty);

        return authType == AuthType.BASIC ? enabled("Basic authentication is used") : disabled("Basic authentication is not set as Basic");
    }

    private ConditionEvaluationResult evaluateForExternalEngine(ApplicationContext applicationContext) {
        ExternalEngine engine = applicationContext.getBean(ControlUiTestingProperties.class).getEngine();
        if (engine == null) {
            return disabled("External engine is not configured");
        }
        return engine.getAuthType() == AuthType.BASIC
                ? enabled("External engine '" + engine.getName() + "' uses Basic authentication")
                : disabled("External engine '" + engine.getName() + "' does not use Basic authentication " +
                        "(authType=" + engine.getAuthType() + ")");
    }

    private ConditionEvaluationResult enabledByDefault() {
        return enabled("Basic authenticated rule is not present");
    }
}
