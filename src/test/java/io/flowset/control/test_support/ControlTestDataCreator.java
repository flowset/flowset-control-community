/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support;

import io.flowset.control.entity.User;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.entity.engine.EnvironmentType;
import io.flowset.control.security.FullAccessRole;
import io.flowset.control.security.UiMinimalRole;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder;
import io.jmix.security.model.ResourceRole;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.RoleModelConverter;
import io.jmix.security.model.RoleSourceType;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securitydata.impl.role.DatabaseRolePersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * For creating test data in the Control database.
 */
@Slf4j
@Component("control_ControlTestDataCreator")
public class ControlTestDataCreator {

    private final SystemAuthenticator systemAuthenticator;
    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AnnotatedRoleBuilder annotatedRoleBuilder;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Autowired
    private DatabaseRolePersistence databaseRolePersistence;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private DataManager dataManager;

    public ControlTestDataCreator(SystemAuthenticator systemAuthenticator) {
        this.systemAuthenticator = systemAuthenticator;
    }

    /**
     * Creates a BPM engine entity with the specified parameters and saves it to the database.
     *
     * @param name      BPM engine name
     * @param baseUrl   a base URL for the BPM engine
     * @param isDefault whether this engine is the default one
     * @return created and saved to the Control database BPM engine entity
     */
    public BpmEngine createBpmEngine(String name, String baseUrl, boolean isDefault) {
        BpmEngine engine = unconstrainedDataManager.create(BpmEngine.class);
        engine.setName(name);
        engine.setType(EngineType.CAMUNDA_7);
        engine.setEnvironmentType(EnvironmentType.LOCAL);
        engine.setBaseUrl(baseUrl);
        engine.setIsDefault(isDefault);

        BpmEngine saved = unconstrainedDataManager.save(engine);

        log.info("Created and saved BPM engine: {}, id: '{}', url: '{}'", saved.getName(), saved.getId(), saved.getBaseUrl());
        return saved;
    }

    /**
     * Creates and saves BPM engine entity with the specified name and random URL.
     *
     * @param name      BPM engine name
     * @param isDefault whether this engine is the default one
     * @return created and saved to the Control database BPM engine
     */
    public BpmEngine createRandomBpmEngine(String name, boolean isDefault) {
        String engineUrl = "http://%s.invalid/engine-rest".formatted(System.currentTimeMillis());
        return createBpmEngine(name, engineUrl, isDefault);
    }

    /**
     * Creates a user with the specified resource-role assignments.
     *
     * @param username            user name
     * @param password            raw user password
     * @param resourceRoleClasses resource role classes to create and assign
     */
    public void createUser(String username, String password, Class<?>... resourceRoleClasses) {
        User user = unconstrainedDataManager.create(User.class);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);

        unconstrainedDataManager.save(user);

        List<RoleAssignmentEntity> assignmentsToSave = Stream.of(resourceRoleClasses)
                .map(this::createDatabaseResourceRole)
                .map(roleCode -> {
                    RoleAssignmentEntity entity = unconstrainedDataManager.create(RoleAssignmentEntity.class);
                    entity.setRoleCode(roleCode);
                    entity.setRoleType(RoleAssignmentRoleType.RESOURCE);
                    entity.setUsername(username);
                    return entity;
                })
                .toList();

        unconstrainedDataManager.saveAll(assignmentsToSave);

        log.info("Created test user '{}' with {} role(s)", username, resourceRoleClasses);
    }

    /**
     * Creates a resource role from the annotated class and saves it to the database.
     *
     * @param resourceRoleClass resource role class
     * @return saved role code
     */
    private String createDatabaseResourceRole(Class<?> resourceRoleClass) {
        if (resourceRoleClass.equals(UiMinimalRole.class) || resourceRoleClass.equals(FullAccessRole.class)) {
            ResourceRole resourceRole = annotatedRoleBuilder.createResourceRole(resourceRoleClass.getName());
            return resourceRole.getCode();
        }

        ResourceRole resourceRole = annotatedRoleBuilder.createResourceRole(resourceRoleClass.getName());
        ResourceRoleModel roleModel = roleModelConverter.createResourceRoleModel(resourceRole);

        roleModel.setSource(RoleSourceType.DATABASE);
        entityStates.setNew(roleModel, true);

        roleModel.getResourcePolicies()
                .forEach(policy -> entityStates.setNew(policy, true));

        systemAuthenticator.runWithSystem(
                () -> databaseRolePersistence.save(roleModel));

        return resourceRole.getCode();
    }
}
