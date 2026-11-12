/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance.impl;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.service.engine.EngineOperationServiceProvider;
import io.flowset.control.service.processinstance.MigrationService;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the {@link MigrationService} of the selected engine type, see {@link EngineOperationServiceProvider}.
 */
@Primary
@Service("control_MigrationService")
public class MigrationServiceImpl implements MigrationService {

    protected final EngineOperationServiceProvider engineOperationServiceProvider;

    public MigrationServiceImpl(EngineOperationServiceProvider engineOperationServiceProvider) {
        this.engineOperationServiceProvider = engineOperationServiceProvider;
    }

    @Override
    public List<String> validateMigrationOfSingleProcessInstance(String processInstanceId,
                                                                 String targetProcessDefinitionId) {
        return engineOperationServiceProvider.getMigrationService()
                .validateMigrationOfSingleProcessInstance(processInstanceId, targetProcessDefinitionId);
    }

    @Override
    public void migrateSingleProcessInstance(String processInstanceId, String targetProcessDefinitionId) {
        engineOperationServiceProvider.getMigrationService()
                .migrateSingleProcessInstance(processInstanceId, targetProcessDefinitionId);
    }

    @Override
    public List<String> validateMigrationOfProcessInstances(String srcProcessDefinitionId,
                                                            String targetProcessDefinitionId) {
        return engineOperationServiceProvider.getMigrationService()
                .validateMigrationOfProcessInstances(srcProcessDefinitionId, targetProcessDefinitionId);
    }

    @Nullable
    @Override
    public BatchData migrateAllProcessInstances(String srcProcessDefinitionId, String targetProcessDefinitionId) {
        return engineOperationServiceProvider.getMigrationService()
                .migrateAllProcessInstances(srcProcessDefinitionId, targetProcessDefinitionId);
    }
}
