/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance.impl;

import feign.utils.ExceptionUtils;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.service.processinstance.MigrationService;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.rest.client.api.MigrationApiClient;
import org.camunda.community.rest.client.model.*;
import org.camunda.community.rest.impl.RemoteRuntimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static io.flowset.control.util.ExceptionUtils.isConnectionError;

@Service("control_MigrationService")
@Slf4j
public class MigrationServiceImpl implements MigrationService {
    protected final MigrationApiClient migrationApiClient;
    protected final RemoteRuntimeService remoteRuntimeService;

    public MigrationServiceImpl(MigrationApiClient migrationApiClient,
                                RemoteRuntimeService remoteRuntimeService) {
        this.migrationApiClient = migrationApiClient;
        this.remoteRuntimeService = remoteRuntimeService;
    }

    @Override
    public List<String> validateMigrationOfSingleProcessInstance(String processInstanceId, String targetProcessDefinitionId) {
        try {
            ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
            return validateMigrationOfProcessInstances(processInstance.getProcessDefinitionId(), targetProcessDefinitionId);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable create process instance migration plan because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void migrateSingleProcessInstance(String processInstanceId, String targetProcessDefinitionId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        MigrationPlanDto migrationPlan = createMigrationPlan(processInstance.getProcessDefinitionId(), targetProcessDefinitionId);
        ResponseEntity<Void> response = migrationApiClient.executeMigrationPlan(new MigrationExecutionDto()
                .migrationPlan(migrationPlan)
                .processInstanceIds(List.of(processInstanceId)));
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Error on process instance migration: process instance {}, target process definition {}", processInstanceId, targetProcessDefinitionId);
        }
    }

    @Override
    public List<String> validateMigrationOfProcessInstances(String srcProcessDefinitionId, String targetProcessDefinitionId) {
        try {
            MigrationPlanDto migrationPlanDto = createMigrationPlan(srcProcessDefinitionId, targetProcessDefinitionId);
            ResponseEntity<MigrationPlanReportDto> response = migrationApiClient.validateMigrationPlan(migrationPlanDto);
            if (response.getStatusCode().is2xxSuccessful()) {
                List<MigrationInstructionValidationReportDto> instructionReports = Optional.ofNullable(response.getBody())
                        .map(MigrationPlanReportDto::getInstructionReports)
                        .orElse(List.of());
                return instructionReports
                        .stream()
                        .flatMap(validationInstruction -> validationInstruction.getFailures().stream())
                        .toList();
            }
            log.error("Error on process instances migration: source process definition {}, target process definition {}", srcProcessDefinitionId, targetProcessDefinitionId);
            return List.of();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable create process definition migration plan because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void migrateAllProcessInstances(String srcProcessDefinitionId, String targetProcessDefinitionId) {
        MigrationPlanDto migrationPlan = createMigrationPlan(srcProcessDefinitionId, targetProcessDefinitionId);

        ProcessInstanceQueryDto procInstancesQuery = new ProcessInstanceQueryDto().processDefinitionId(srcProcessDefinitionId);
        MigrationExecutionDto migrationDto = new MigrationExecutionDto()
                .migrationPlan(migrationPlan)
                .processInstanceQuery(procInstancesQuery);
        ResponseEntity<BatchDto> response = migrationApiClient.executeMigrationPlanAsync(migrationDto);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Error on starting async process instance migration: source process definition {}, target process definition {}", srcProcessDefinitionId, targetProcessDefinitionId);
        }
    }

    protected ProcessInstance getProcessInstanceById(String processInstanceId) {
        return remoteRuntimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    @Nullable
    protected MigrationPlanDto createMigrationPlan(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
        MigrationPlanGenerationDto migrationDto = new MigrationPlanGenerationDto();
        migrationDto.setSourceProcessDefinitionId(sourceProcessDefinitionId);
        migrationDto.setTargetProcessDefinitionId(targetProcessDefinitionId);
        ResponseEntity<MigrationPlanDto> migrationPlanDtoResponseEntity = migrationApiClient.generateMigrationPlan(migrationDto);
        if (migrationPlanDtoResponseEntity.getStatusCode().is2xxSuccessful() && migrationPlanDtoResponseEntity.getBody() != null) {
            return migrationPlanDtoResponseEntity.getBody();
        }
        log.error("Error on generating migration plan: source process definition {}, target process definition {}", sourceProcessDefinitionId, targetProcessDefinitionId);

        return null;
    }
}
