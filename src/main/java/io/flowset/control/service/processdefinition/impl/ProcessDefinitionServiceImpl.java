/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition.impl;

import feign.FeignException;
import feign.utils.ExceptionUtils;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.jmix.core.Sort;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.mapper.ProcessDefinitionMapper;
import io.flowset.control.service.engine.EngineTenantProvider;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient;
import org.camunda.community.rest.client.model.ProcessDefinitionDiagramDto;
import org.camunda.community.rest.client.model.ProcessDefinitionSuspensionStateDto;
import org.camunda.community.rest.impl.RemoteRepositoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.flowset.control.util.ExceptionUtils.isConnectionError;
import static io.flowset.control.util.QueryUtils.*;

@Service("control_ProcessDefinitionService")
@Slf4j
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {
    protected final RemoteRepositoryService remoteRepositoryService;
    protected final ProcessDefinitionMapper processDefinitionMapper;
    protected final ProcessDefinitionApiClient processDefinitionApiClient;
    protected final EngineTenantProvider engineTenantProvider;

    public ProcessDefinitionServiceImpl(RemoteRepositoryService remoteRepositoryService,
                                        ProcessDefinitionMapper processDefinitionMapper,
                                        ProcessDefinitionApiClient processDefinitionApiClient,
                                        EngineTenantProvider engineTenantProvider) {
        this.remoteRepositoryService = remoteRepositoryService;
        this.processDefinitionMapper = processDefinitionMapper;
        this.processDefinitionApiClient = processDefinitionApiClient;
        this.engineTenantProvider = engineTenantProvider;
    }

    @Override
    public List<ProcessDefinitionData> findLatestVersions() {
        try {
            return createProcessDefinitionQuery()
                    .orderByProcessDefinitionVersion()
                    .asc()
                    .latestVersion()
                    .list()
                    .stream()
                    .map(processDefinitionMapper::fromProcessDefinitionModel)
                    .toList();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load latest versions of process definitions because BPM engine not selected");
                return List.of();
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load latest versions of process definitions because of connection error: ", e);
                return List.of();
            }
            throw e;
        }
    }


    @Override
    public List<ProcessDefinitionData> findAllByKey(String processDefinitionKey) {
        try {
            return createProcessDefinitionQuery()
                    .orderByProcessDefinitionVersion()
                    .asc()
                    .processDefinitionKey(processDefinitionKey)
                    .list()
                    .stream()
                    .map(processDefinitionMapper::fromProcessDefinitionModel)
                    .toList();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load process definition versions by key because BPM engine not selected");
                return List.of();
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load process definition versions by key because of connection error: ", e);
                return List.of();
            }
            throw e;
        }
    }

    @Override
    public List<ProcessDefinitionData> findAll(ProcessDefinitionLoadContext context) {
        try {
            ProcessDefinitionQuery processDefinitionQuery = createProcessDefinitionQuery(context.getFilter(), context.getSort());

            List<ProcessDefinition> processDefinitions;
            if (context.getFirstResult() != null && context.getMaxResults() != null) {
                processDefinitions = processDefinitionQuery.listPage(context.getFirstResult(), context.getMaxResults());
            } else {
                processDefinitions = processDefinitionQuery.list();
            }

            return processDefinitions
                    .stream()
                    .map(processDefinitionMapper::fromProcessDefinitionModel)
                    .toList();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load process definitions because BPM engine not selected");
                return List.of();
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load process definitions because of connection error: ", e);
                throw new EngineConnectionFailedException(rootCause.getMessage(), -1, rootCause.getMessage());
            }
            throw e;
        }
    }

    @Override
    public long getCount(@Nullable ProcessDefinitionFilter filter) {
        try {
            ProcessDefinitionQuery processDefinitionQuery = createProcessDefinitionQuery(filter, null);
            return processDefinitionQuery.count();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load get count of process definitions because BPM engine not selected");
                return 0;
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load get count of process definitions because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public ProcessDefinitionData getById(String processDefinitionId) {
        try {
            ProcessDefinition processDefinition = createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();
            return processDefinitionMapper.fromProcessDefinitionModel(processDefinition);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load process definition by id '{}' because BPM engine not selected", processDefinitionId);
                return null;
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable load process definition by id '{}' because of connection error: ", processDefinitionId, e);
                throw new EngineConnectionFailedException(rootCause.getMessage(), -1, rootCause.getMessage());
            }
            throw e;
        }
    }

    @Override
    public String getBpmnXml(String processDefinitionId) {
        try {
            ResponseEntity<ProcessDefinitionDiagramDto> processDefinitionBpmn20Xml = processDefinitionApiClient.getProcessDefinitionBpmn20Xml(processDefinitionId);
            if (processDefinitionBpmn20Xml.getStatusCode().is2xxSuccessful()) {
                ProcessDefinitionDiagramDto diagramDto = processDefinitionBpmn20Xml.getBody();
                return diagramDto != null ? diagramDto.getBpmn20Xml() : null;
            }
            return null;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load process definition XML by id '{}' because BPM engine not selected", processDefinitionId);
                return null;
            }

            if (isConnectionError(rootCause)) {
                log.error("Unable load process definition XML by id '{}' because of connection error: ", processDefinitionId, e);
                return null;
            }

            if (rootCause instanceof FeignException feignException && feignException.status() == 404) {
                log.warn("Unable to load process definition XML by id '{}' because process does not exist", processDefinitionId);
                return null;
            }

            throw e;
        }
    }

    @Override
    public void activateById(String processDefinitionId, boolean activateBelongsInstances) {
        try {
            suspendOrActivateProcessDefinitionById(processDefinitionId, activateBelongsInstances, false);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to activate process definition by id '{}' because of connection error: ", processDefinitionId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void activateAllVersionsByKey(String processDefinitionKey, boolean activateBelongsInstances) {
        try {
            suspendOrActivateProcessDefinitionsByKey(processDefinitionKey, activateBelongsInstances, false);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to activate process definition by key '{}' because of connection error: ", processDefinitionKey, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void suspendById(String processDefinitionId, boolean suspendBelongsInstances) {
        try {
            suspendOrActivateProcessDefinitionById(processDefinitionId, suspendBelongsInstances, true);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to suspend process definition by id '{}' because of connection error: ", processDefinitionId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void suspendAllVersionsByKey(String processDefinitionKey, boolean suspendBelongsInstances) {
        try {
            suspendOrActivateProcessDefinitionsByKey(processDefinitionKey, suspendBelongsInstances, true);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to suspend process definition by key '{}' because of connection error: ", processDefinitionKey, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void deleteAllVersionsByKey(String processDefinitionKey, boolean deleteAllRelatedInstances) {
        try {
            boolean hasNextVersion = deleteProcessVersionByKey(processDefinitionKey, deleteAllRelatedInstances);
            if (hasNextVersion) {
                do {
                    hasNextVersion = deleteProcessVersionByKey(processDefinitionKey, deleteAllRelatedInstances);
                } while (hasNextVersion);
            }
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to delete process definition by key '{}' because of connection error: ", processDefinitionKey, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Delete last process definition version by the provided key.
     *
     * @param processDefinitionKey      process definition key
     * @param deleteAllRelatedInstances where remove or not related process instances
     * @return true if version for the provided key does not exist (response = 404), false - otherwise.
     */
    protected boolean deleteProcessVersionByKey(String processDefinitionKey, boolean deleteAllRelatedInstances) {
        try {
            processDefinitionApiClient.deleteProcessDefinitionsByKey(processDefinitionKey, deleteAllRelatedInstances, null, null);
            return true;
        } catch (Exception e) {
            if (e instanceof FeignException feignException && feignException.status() == 404) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public void deleteById(String processDefinitionId, boolean deleteAllRelatedInstances) {
        try {
            remoteRepositoryService.deleteProcessDefinition(processDefinitionId, deleteAllRelatedInstances);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to delete process definition by id '{}' because of connection error: ", processDefinitionId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @SuppressWarnings("ConstantValue")
    protected void suspendOrActivateProcessDefinitionById(String processDefinitionId,
                                                          boolean cascadeOperationToBelongsInstances,
                                                          boolean suspended) {
        ProcessDefinitionSuspensionStateDto suspendedCommand = new ProcessDefinitionSuspensionStateDto()
                .suspended(suspended);
        if (cascadeOperationToBelongsInstances) {
            suspendedCommand.includeProcessInstances(cascadeOperationToBelongsInstances);
        }
        processDefinitionApiClient.updateProcessDefinitionSuspensionStateById(processDefinitionId, suspendedCommand);
    }

    @SuppressWarnings("ConstantValue")
    protected void suspendOrActivateProcessDefinitionsByKey(String processDefinitionKey,
                                                            boolean cascadeOperationToBelongsInstances,
                                                            boolean suspended) {
        ProcessDefinitionSuspensionStateDto suspendedCommand = new ProcessDefinitionSuspensionStateDto()
                .processDefinitionKey(processDefinitionKey)
                .suspended(suspended);
        if (cascadeOperationToBelongsInstances) {
            suspendedCommand.includeProcessInstances(cascadeOperationToBelongsInstances);
        }
        processDefinitionApiClient.updateProcessDefinitionSuspensionState(suspendedCommand);
    }

    protected ProcessDefinitionQuery createProcessDefinitionQuery(@Nullable ProcessDefinitionFilter filter, @Nullable Sort sort) {
        ProcessDefinitionQuery processDefinitionQuery = createProcessDefinitionQuery();

        addDefinitionFilters(processDefinitionQuery, filter);
        addDefinitionSort(processDefinitionQuery, sort);
        return processDefinitionQuery;
    }

    protected ProcessDefinitionQuery createProcessDefinitionQuery() {
        ProcessDefinitionQuery processDefinitionQuery = remoteRepositoryService.createProcessDefinitionQuery();

        addTenant(processDefinitionQuery, engineTenantProvider::getCurrentUserTenantId);
        return processDefinitionQuery;
    }
}
