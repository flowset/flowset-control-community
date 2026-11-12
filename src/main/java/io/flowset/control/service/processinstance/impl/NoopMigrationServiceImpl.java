/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance.impl;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.service.processinstance.MigrationService;
import io.flowset.control.service.engine.EngineServiceDelegate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serves {@link MigrationService} calls when no BPM engine is selected: every operation throws
 * {@link EngineNotSelectedException}.
 */
@EngineServiceDelegate
@Service("control_NoopMigrationService")
public class NoopMigrationServiceImpl implements MigrationService {

    @Override
    public List<String> validateMigrationOfSingleProcessInstance(String processInstanceId,
                                                                 String targetProcessDefinitionId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public void migrateSingleProcessInstance(String processInstanceId, String targetProcessDefinitionId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public List<String> validateMigrationOfProcessInstances(String srcProcessDefinitionId,
                                                            String targetProcessDefinitionId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }

    @Override
    public BatchData migrateAllProcessInstances(String srcProcessDefinitionId, String targetProcessDefinitionId) {
        throw new EngineNotSelectedException("BPM engine not selected");
    }
}
