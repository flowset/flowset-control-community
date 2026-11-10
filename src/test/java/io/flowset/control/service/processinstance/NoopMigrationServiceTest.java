/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class NoopMigrationServiceTest extends AbstractIntegrationTest {

    @Autowired
    MigrationService migrationService;

    @Test
    @DisplayName("EngineNotSelectedException thrown when validate single instance migration if no engine exists")
    void givenNoEngine_whenValidateMigrationOfSingleProcessInstance_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> migrationService.validateMigrationOfSingleProcessInstance("instanceId",
                "targetProcessDefinitionId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when migrate single instance if no engine exists")
    void givenNoEngine_whenMigrateSingleProcessInstance_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> migrationService.migrateSingleProcessInstance("instanceId",
                "targetProcessDefinitionId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when validate instances migration if no engine exists")
    void givenNoEngine_whenValidateMigrationOfProcessInstances_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> migrationService.validateMigrationOfProcessInstances("srcProcessDefinitionId",
                "targetProcessDefinitionId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when migrate all instances if no engine exists")
    void givenNoEngine_whenMigrateAllProcessInstances_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> migrationService.migrateAllProcessInstances("srcProcessDefinitionId",
                "targetProcessDefinitionId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }
}
