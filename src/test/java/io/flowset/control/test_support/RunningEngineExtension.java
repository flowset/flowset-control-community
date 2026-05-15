/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.test_support.property.ControlEngineTestingProperties;
import io.jmix.core.UnconstrainedDataManager;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.test_support.camunda7.CamundaRunContainer;
import io.flowset.control.test_support.camunda7.OperatonContainer;
import io.flowset.control.test_support.testcontainers.ContainerWrapper;
import io.flowset.control.test_support.testcontainers.EngineContainer;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ModifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static io.flowset.control.test_support.Constants.*;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;

/**
 * JUnit extension that provides the following support BPM engine test container that is used in the tests:
 * <ol>
 *     <li>Creates an engine container from the {@link ControlEngineTestingProperties} or from the {@link WithRunningEngine} attribute values.</li>
 *     <li>Starts a created engine container before all test methods or before each test method.</li>
 *     <li>Cleans engine data after each test method if {@link WithRunningEngine#shared()} is set to true</li>
 *     <li>Creates a {@link BpmEngine} for the running engine container and makes it as selected if {@link WithRunningEngine#selected()} is set to true</li>
 * </ol>
 * <p>
 */
public class RunningEngineExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {
    protected static final Logger log = LoggerFactory.getLogger(RunningEngineExtension.class);

    protected static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(RunningEngineExtension.class);

    protected static final String SHARED_ENGINE_KEY_PREFIX = "sharedEngine";
    protected static final String LOCAL_ENGINE_KEY_PREFIX = "localEngine";
    protected static final String SELECTED_ENGINE_ID = "selectedEngineId";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getTestClass().orElseThrow(() -> new ExtensionConfigurationException("RunningEngineExtension is only supported for test classes"));

        boolean isSharedEngineEnabled = isSharedContainerEnabled(context);
        if (isSharedEngineEnabled) {
            ExtensionContext.Store store = context.getStore(NAMESPACE);

            ContainerWrapper<?> engineContainerWrapper = store.getOrComputeIfAbsent(SHARED_ENGINE_KEY_PREFIX, k -> createContainer(context), ContainerWrapper.class);
            EngineContainer<?> engineContainer = engineContainerWrapper.getContainer();
            injectRunningEngineField(testClass, null, ModifierSupport::isStatic, engineContainer);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        boolean isSharedEngineEnabled = isSharedContainerEnabled(context);
        boolean isSelectedEngine = isSelected(context);

        if (!isSharedEngineEnabled) {
            Class<?> testClass = context.getRequiredTestClass();
            ExtensionContext.Store store = context.getStore(NAMESPACE);

            ContainerWrapper<?> containerWrapper = initLocalContainer(context, store, testClass);
            if (isSelectedEngine) {
                createAndSelectEngine(context, containerWrapper.getContainer());
            }
        } else if (isSelectedEngine) {
            findSharedEngine(context)
                    .ifPresent(engineContainer -> {
                        if (!engineContainer.isRunning()) { //start if engine container is stopped during test execution
                            engineContainer.start();
                            injectRunningEngineField(context.getRequiredTestClass(), null, ModifierSupport::isStatic, engineContainer);
                        }
                        createAndSelectEngine(context, engineContainer);
                    });
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        removeSelectedEngine(context);

        boolean isSharedEngineEnabled = isSharedContainerEnabled(context);
        if (isSharedEngineEnabled) {
            cleanEngineData(context);
        }
    }

    protected boolean isSharedContainerEnabled(ExtensionContext context) {
        return findWithRunningEngine(context).map(WithRunningEngine::shared).orElse(false);
    }

    protected void cleanEngineData(ExtensionContext context) {
        findSharedEngine(context)
                .ifPresent(engineContainer -> {
                            ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
                            applicationContext.getBeansOfType(EngineDataCleaner.class)
                                    .values()
                                    .stream()
                                    .filter(engineDataCleaner -> engineDataCleaner.supports(engineContainer))
                                    .findFirst()
                                    .ifPresent(engineDataCleaner -> engineDataCleaner.clean(engineContainer));
                        }
                );
    }

    protected void createAndSelectEngine(ExtensionContext context, EngineContainer<?> engineContainer) {
        BpmEngine bpmEngine = createAndSaveBpmEngine(context, engineContainer);

        EngineService engineService = getBean(context, EngineService.class);
        engineService.setSelectedEngine(bpmEngine);

        context.getStore(NAMESPACE).put(SELECTED_ENGINE_ID, bpmEngine.getId());
    }

    protected BpmEngine createAndSaveBpmEngine(ExtensionContext context, EngineContainer<?> engineContainer) {
        UnconstrainedDataManager dataManager = getBean(context, UnconstrainedDataManager.class);
        BpmEngine bpmEngine = dataManager.create(BpmEngine.class);
        bpmEngine.setName("Test engine " + UUID.randomUUID());

        bpmEngine.setType(engineContainer.getEngineType());
        bpmEngine.setBaseUrl(engineContainer.getRestBaseUrl());

        AuthType authType = engineContainer.getAuthType();
        bpmEngine.setAuthType(authType);
        bpmEngine.setAuthEnabled(authType != null);

        if (authType == AuthType.BASIC) {
            bpmEngine.setBasicAuthUsername(engineContainer.getBasicAuthUsername());
            bpmEngine.setBasicAuthPassword(engineContainer.getBasicAuthPassword());
        } else if (authType == AuthType.HTTP_HEADER) {
            bpmEngine.setHttpHeaderName(engineContainer.getAuthHeaderName());
            bpmEngine.setHttpHeaderValue(engineContainer.getAuthHeaderValue());
        }

        bpmEngine = dataManager.save(bpmEngine);

        return bpmEngine;
    }

    protected void removeSelectedEngine(ExtensionContext context) {
        UUID selectedEngineId = context.getStore(NAMESPACE).get(SELECTED_ENGINE_ID, UUID.class);
        if (selectedEngineId != null) {
            JdbcTemplate jdbcTemplate = getBean(context, JdbcTemplate.class);
            jdbcTemplate.execute("DELETE FROM control_bpm_engine WHERE id = '" + selectedEngineId + "'");
        }
    }

    protected ContainerWrapper<?> createContainer(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        Environment environment = applicationContext.getEnvironment();
        EngineContainer<?> engineContainer;
        if (environment.matchesProfiles(TEST_ENGINE_PROFILE)) {
            ControlEngineTestingProperties testingProperties = applicationContext.getBean(ControlEngineTestingProperties.class);
            engineContainer = createContainerFromProperties(testingProperties);
        } else {
            engineContainer = createContainerFromAnnotations(context);
        }

        ContainerWrapper<?> containerWrapper = new ContainerWrapper<>(engineContainer);
        containerWrapper.start();

        return containerWrapper;
    }


    protected EngineContainer<?> createContainerFromProperties(ControlEngineTestingProperties controlEngineTestingProperties) {
        String imageName = controlEngineTestingProperties.getDockerImage();

        EngineContainer<?> engineContainer = createContainerByImage(imageName);

        String authTypeString = controlEngineTestingProperties.getAuthType();
        if (authTypeString != null) {
            AuthType authType = AuthType.fromId(authTypeString);
            if (authType == AuthType.BASIC) {
                addBasicAuthentication(engineContainer);
            }
        }

        log.info("Created engine container from application properties: image {}, auth type {}", imageName, authTypeString);

        return engineContainer;
    }

    protected EngineContainer<?> createContainerFromAnnotations(ExtensionContext context) {
        String imageName = findWithRunningEngine(context).map(WithRunningEngine::dockerImageName).orElse(null);
        if (imageName == null) {
            throw new ExtensionConfigurationException("dockerImageName is not set in @WithRunningEngine annotation for test");
        }

        EngineContainer<?> engineContainer = createContainerByImage(imageName);

        AuthType authType = null;
        Optional<EnabledOnBasicAuthentication> basicAuthenticationEnabled = findEnabledOnBasicAuthentication(context);
        if (basicAuthenticationEnabled.isPresent()) {
            authType = AuthType.BASIC;
            addBasicAuthentication(engineContainer);
        }

        log.info("Created engine container from test class annotations: image {}, auth type: {}", imageName, authType);
        return engineContainer;
    }

    protected void addBasicAuthentication(EngineContainer<?> engineContainer) {
        engineContainer.withAuthType(AuthType.BASIC)
                .withBasicAuthUsername(DEFAULT_BASIC_AUTH_USERNAME)
                .withBasicAuthPassword(DEFAULT_BASIC_AUTH_PASSWORD);
    }

    protected EngineContainer<?> createContainerByImage(String dockerImageName) {
        DockerImageName imageName = DockerImageName.parse(dockerImageName);
        if (imageName.isCompatibleWith(CamundaRunContainer.DEFAULT_IMAGE_NAME)) {
            return new CamundaRunContainer(imageName);
        } else if (imageName.isCompatibleWith(OperatonContainer.DEFAULT_IMAGE_NAME)) {
            return new OperatonContainer(imageName);
        }

        throw new ExtensionConfigurationException("RunningEngineExtension is supported only for known docker images of BPM engines");
    }

    protected void injectRunningEngineField(Class<?> testClass, @Nullable Object testInstance,
                                            Predicate<Field> predicate, EngineContainer<?> fieldValue) {

        predicate = predicate.and(field -> isEngineContainer(field.getType()));
        findAnnotatedFields(testClass, RunningEngine.class, predicate)
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        field.set(testInstance, fieldValue);
                    } catch (IllegalAccessException e) {
                        log.error("Unable to set running engine container in field {} in test class {}", field.getName(), testClass, e);
                    }
                });
    }

    protected static boolean isEngineContainer(Class<?> type) {
        return type == EngineContainer.class || type.getSuperclass() == EngineContainer.class;
    }

    protected <V> V getBean(ExtensionContext context, Class<V> beanClass) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        return applicationContext.getBean(beanClass);
    }

    protected boolean isSelected(ExtensionContext context) {
        return findWithRunningEngine(context).map(WithRunningEngine::selected).orElse(false);
    }

    protected Optional<WithRunningEngine> findWithRunningEngine(ExtensionContext context) {
        Optional<ExtensionContext> current = Optional.of(context);
        while (current.isPresent()) {
            Optional<WithRunningEngine> withRunningEngine = AnnotationSupport.findAnnotation(
                    current.get().getRequiredTestClass(),
                    WithRunningEngine.class
            );
            if (withRunningEngine.isPresent()) {
                return withRunningEngine;
            }
            current = current.get().getParent();
        }
        return Optional.empty();
    }

    protected Optional<EnabledOnBasicAuthentication> findEnabledOnBasicAuthentication(ExtensionContext context) {
        return AnnotationSupport.findAnnotation(context.getRequiredTestClass(), EnabledOnBasicAuthentication.class);
    }

    protected Optional<EngineContainer<?>> findSharedEngine(ExtensionContext context) {
        Optional<ExtensionContext> current = Optional.of(context);
        while (current.isPresent()) {
            ExtensionContext.Store store = current.get().getStore(NAMESPACE);
            ContainerWrapper<?> containerWrapper = store.get(SHARED_ENGINE_KEY_PREFIX, ContainerWrapper.class);

            if (containerWrapper != null) {
                return Optional.ofNullable(containerWrapper.getContainer());
            }
            current = current.get().getParent();
        }
        return Optional.empty();
    }

    protected ContainerWrapper<?> initLocalContainer(ExtensionContext context, ExtensionContext.Store store, Class<?> testClass) {
        ContainerWrapper<?> engineContainerWrapper = store.getOrComputeIfAbsent(LOCAL_ENGINE_KEY_PREFIX, k -> createContainer(context), ContainerWrapper.class);

        EngineContainer<?> engineContainer = engineContainerWrapper.getContainer();
        Object testInstance = context.getRequiredTestInstance();
        injectRunningEngineField(testClass, testInstance, ModifierSupport::isNotStatic, engineContainer);

        return engineContainerWrapper;
    }
}
