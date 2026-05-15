/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.engine.external;

import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EnvironmentType;
import io.flowset.control.test_support.camunda7.CamundaDataCleaner;
import io.flowset.control.test_support.property.ControlUiTestingProperties;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.data.PersistenceHints;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ModifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;

/**
 * JUnit extension that provides the following support external BPM engine that is used in the tests:
 * <ol>
 *     <li>Creates an engine container from the {@link ControlUiTestingProperties}</li>
 *     <li>Injects external engine data for the field with annotation {@link RunningExternalEngine}</li>
 *     <li>Cleans engine data after each test method</li>
 *     <li>Creates a {@link BpmEngine} for the external engine data and saves it to the database
 *         if {@link WithRunningExternalEngine#save()} is set as true</li>
 * </ol>
 */
public class ExternalRunningEngineExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(ExternalRunningEngineExtension.class);

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ExternalRunningEngineExtension.class);

    private static final String INSERTED_ENGINE_ID = "insertedEngineId";

    @Override
    public void beforeAll(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        ExternalEngine engine = applicationContext.getBean(ControlUiTestingProperties.class).getEngine();
        injectFields(context.getRequiredTestClass(), null, ModifierSupport::isStatic, engine);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        ExternalEngine externalEngine = applicationContext.getBean(ControlUiTestingProperties.class).getEngine();

        injectFields(context.getRequiredTestClass(), context.getRequiredTestInstance(),
                ModifierSupport::isNotStatic, externalEngine);

        Optional<WithRunningExternalEngine> annotation = findAnnotation(context);
        if (annotation.isEmpty() || !annotation.get().save()) {
            return;
        }

        UnconstrainedDataManager dataManager = applicationContext.getBean(UnconstrainedDataManager.class);

        String baseUrl = externalEngine.getRestBaseUrl();
        List<BpmEngine> existingEngines = dataManager.load(BpmEngine.class)
                .condition(PropertyCondition.equal("baseUrl", baseUrl))
                .hint(PersistenceHints.SOFT_DELETION, false)
                .list();
        if (!existingEngines.isEmpty()) {
            removeBpmEngines(dataManager, existingEngines);
            log.info("Deleted '{}' existing BpmEngine(s) with baseUrl={}", existingEngines.size(), baseUrl);
        }

        BpmEngine bpmEngine = createBpmEngine(dataManager, externalEngine);
        bpmEngine = dataManager.save(bpmEngine);

        setInsertedEngineId(context, bpmEngine.getId());
        log.info("Saved BpmEngine id={}, name={}, baseUrl={}, isDefault={}",
                bpmEngine.getId(), bpmEngine.getName(), bpmEngine.getBaseUrl(), bpmEngine.getIsDefault());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Optional<WithRunningExternalEngine> annotation = findAnnotation(context);
        if (annotation.isEmpty()) {
            return;
        }

        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        ControlUiTestingProperties props = applicationContext.getBean(ControlUiTestingProperties.class);
        ExternalEngine externalEngine = props.getEngine();

        CamundaDataCleaner cleaner = applicationContext.getBean(CamundaDataCleaner.class);
        try {
            cleaner.clean(externalEngine);
        } catch (Exception e) {
            log.error("Failed to clean external Camunda engine data after test", e);
        }

        UUID insertedEngineId = getInsertedEngineId(context);
        if (insertedEngineId != null) {
            UnconstrainedDataManager dataManager = applicationContext.getBean(UnconstrainedDataManager.class);
            try {
                dataManager.load(BpmEngine.class)
                        .id(insertedEngineId)
                        .hint(PersistenceHints.SOFT_DELETION, false)
                        .optional()
                        .ifPresent(bpmEngine -> removeBpmEngines(dataManager, List.of(bpmEngine)));
            } catch (Exception e) {
                log.error("Failed to remove BpmEngine id={}", insertedEngineId, e);
            }
            removeInsertedEngineId(context);
        }
    }

    /**
     * Removes (hard-delete) the specified BPM engines from the database.
     *
     * @param dataManager data manager
     * @param bpmEngines  BPM engines to remove
     */
    private void removeBpmEngines(UnconstrainedDataManager dataManager, List<BpmEngine> bpmEngines) {
        SaveContext saveContext = new SaveContext()
                .removing(bpmEngines)
                .setDiscardSaved(true)
                .setHint(PersistenceHints.SOFT_DELETION, false);

        dataManager.save(saveContext);
    }

    /**
     * Injects the specified external engine into the fields of the specified test class or
     * test instance that have the {@link RunningExternalEngine} annotation.
     *
     * @param testClass      test class
     * @param testInstance   test instance
     * @param modifierFilter field modifier filter
     * @param engine         external engine
     */
    private void injectFields(Class<?> testClass, @Nullable Object testInstance,
                              Predicate<Field> modifierFilter, ExternalEngine engine) {
        Predicate<Field> filter = modifierFilter.and(f -> f.getType().isAssignableFrom(ExternalEngine.class));
        findAnnotatedFields(testClass, RunningExternalEngine.class, filter)
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        field.set(testInstance, engine);
                    } catch (IllegalAccessException e) {
                        log.error("Unable to set ExternalEngine in field {} in test class {}",
                                field.getName(), testClass.getName(), e);
                    }
                });
    }

    /**
     * Creates a BPM engine entity from the specified external engine data.
     *
     * @param dataManager data manager
     * @param engine      external engine data
     * @return BPM engine entity
     */
    private BpmEngine createBpmEngine(UnconstrainedDataManager dataManager, ExternalEngine engine) {
        BpmEngine bpmEngine = dataManager.create(BpmEngine.class);

        String name = engine.getName();
        bpmEngine.setName(StringUtils.isBlank(name) ? "Test engine " + UUID.randomUUID() : name);
        bpmEngine.setEnvironmentType(EnvironmentType.LOCAL);

        bpmEngine.setType(engine.getType());
        bpmEngine.setBaseUrl(engine.getRestBaseUrl());
        bpmEngine.setIsDefault(true);

        AuthType authType = engine.getAuthType();
        bpmEngine.setAuthType(authType);
        bpmEngine.setAuthEnabled(authType != null);

        if (authType == AuthType.BASIC) {
            bpmEngine.setBasicAuthUsername(engine.getBasicAuthUsername());
            bpmEngine.setBasicAuthPassword(engine.getBasicAuthPassword());
        } else if (authType == AuthType.HTTP_HEADER) {
            bpmEngine.setHttpHeaderName(engine.getAuthHeaderName());
            bpmEngine.setHttpHeaderValue(engine.getAuthHeaderValue());
        }
        return bpmEngine;
    }

    private Optional<WithRunningExternalEngine> findAnnotation(ExtensionContext context) {
        Optional<ExtensionContext> current = Optional.of(context);
        while (current.isPresent()) {
            Optional<WithRunningExternalEngine> annotation = AnnotationSupport.findAnnotation(
                    current.get().getRequiredTestClass(), WithRunningExternalEngine.class);
            if (annotation.isPresent()) {
                return annotation;
            }
            current = current.get().getParent();
        }
        return Optional.empty();
    }

    @Nullable
    private UUID getInsertedEngineId(ExtensionContext context) {
        return context.getStore(NAMESPACE).get(INSERTED_ENGINE_ID, UUID.class);
    }

    private void setInsertedEngineId(ExtensionContext context, UUID id) {
        context.getStore(NAMESPACE).put(INSERTED_ENGINE_ID, id);
    }

    private void removeInsertedEngineId(ExtensionContext context) {
        context.getStore(NAMESPACE).remove(INSERTED_ENGINE_ID);
    }
}
