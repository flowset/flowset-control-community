/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.dashboard.impl;

import feign.FeignException;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.event.EntityChangedEvent;
import io.flowset.control.entity.ProcessExecutionGraphEntry;
import io.flowset.control.entity.dashboard.ProcessDefinitionStatistics;
import io.flowset.control.entity.engine.AuthType;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.mapper.ProcessDefinitionMapper;
import io.flowset.control.property.UiProperties;
import io.flowset.control.restsupport.FeignClientCreationContext;
import io.flowset.control.restsupport.FeignClientProvider;
import io.flowset.control.service.dashboard.DashboardService;
import io.flowset.control.service.engine.EngineTenantProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient;
import org.camunda.community.rest.client.api.ProcessInstanceApiClient;
import org.camunda.community.rest.client.api.TaskApiClient;
import org.camunda.community.rest.client.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.flowset.control.util.EngineRestUtils.getCountResult;
import static io.flowset.control.util.ExceptionUtils.isConnectionError;
import static io.flowset.control.util.QueryUtils.addTenant;

@Service("control_DashboardService")
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    protected final Metadata metadata;
    protected final FeignClientProvider feignClientProvider;
    protected final ProcessDefinitionMapper processDefinitionMapper;
    protected final UiProperties uiProperties;
    protected final EngineTenantProvider engineTenantProvider;

    protected Map<UUID, TaskApiClient> taskClientByEngineId = new ConcurrentHashMap<>();
    protected Map<UUID, ProcessDefinitionApiClient> processDefinitionClientByEngineId = new ConcurrentHashMap<>();
    protected Map<UUID, ProcessInstanceApiClient> processInstanceClientByEngineId = new ConcurrentHashMap<>();
    protected Map<UUID, HistoryApiClient> historyApiClientByEngineId = new ConcurrentHashMap<>();

    public DashboardServiceImpl(Metadata metadata, FeignClientProvider feignClientProvider,
                                ProcessDefinitionMapper processDefinitionMapper,
                                UiProperties uiProperties,
                                EngineTenantProvider engineTenantProvider) {
        this.metadata = metadata;
        this.feignClientProvider = feignClientProvider;
        this.processDefinitionMapper = processDefinitionMapper;
        this.uiProperties = uiProperties;
        this.engineTenantProvider = engineTenantProvider;
    }

    @Override
    public List<ProcessExecutionGraphEntry> getRecentActivityStatistics(BpmEngine bpmEngine) {
        OffsetDateTime from = OffsetDateTime.now().minusDays(uiProperties.getRecentActivityDays()).with(LocalTime.MIN);
        OffsetDateTime to = OffsetDateTime.now();

        HistoryApiClient historyApiClient = historyApiClientByEngineId
                .computeIfAbsent(bpmEngine.getId(), engineId -> createClient(bpmEngine, HistoryApiClient.class));

        List<HistoricProcessInstanceDto> startedInstances = loadStartedInstances(from, to, historyApiClient);
        List<HistoricProcessInstanceDto> finishedInstances = loadFinishedInstances(from, to, historyApiClient);

        return createWeeklyStatistics(from, to, startedInstances, finishedInstances);
    }

    @Override
    public long getUserTasksCount(BpmEngine bpmEngine) {
        TaskApiClient taskApiClient = taskClientByEngineId
                .computeIfAbsent(bpmEngine.getId(), engineId -> createClient(bpmEngine, TaskApiClient.class));
        try {
            ResponseEntity<CountResultDto> response = taskApiClient.queryTasksCount(createTaskQueryDto());
            if (response.getStatusCode().is2xxSuccessful()) {
                return getCountResult(response.getBody());
            }
            log.error("Error while user task count loading, status code {}", response.getStatusCode());
            return 0;
        } catch (FeignException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to load user tasks count because of connection error: {} ", e.getMessage());
            } else {
                log.error("Error while user task count loading ", e);
            }
            return 0;
        }
    }

    @Override
    public long getDeployedProcessesCount(BpmEngine bpmEngine) {
        ProcessDefinitionApiClient processDefinitionApiClient = processDefinitionClientByEngineId
                .computeIfAbsent(bpmEngine.getId(), engineId -> createClient(bpmEngine, ProcessDefinitionApiClient.class));
        try {
            String tenantId = engineTenantProvider.getCurrentUserTenantId();
            ResponseEntity<CountResultDto> response = processDefinitionApiClient.getProcessDefinitionsCount(null,
                    null, null, null, null,
                    null, null, null, null, null,
                    null, null, null, true,
                    null, null, null, null, null, null,
                    null, null, null, tenantId, null,
                    null, null, null,
                    null, null, null, null);

            if (response.getStatusCode().is2xxSuccessful()) {
                return getCountResult(response.getBody());
            }
            log.error("Error while deployed processes count loading, status code {}", response.getStatusCode());
            return 0;
        } catch (FeignException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to load deployed processes count because of connection error: {} ", e.getMessage());
            } else {
                log.error("Error while deployed processes count loading ", e);
            }
            return 0;
        }
    }

    @Override
    public long getRunningProcessCount(BpmEngine bpmEngine) {
        ProcessInstanceApiClient processInstanceApiClient = processInstanceClientByEngineId
                .computeIfAbsent(bpmEngine.getId(), engineId -> createClient(bpmEngine, ProcessInstanceApiClient.class));
        try {
            ResponseEntity<CountResultDto> response = processInstanceApiClient.queryProcessInstancesCount(createProcessInstanceQueryDto()
                    .active(true));

            if (response.getStatusCode().is2xxSuccessful()) {
                return getCountResult(response.getBody());
            }
            log.error("Error while running process instances count loading, status code {}", response.getStatusCode());
            return 0;
        } catch (FeignException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Error while running process instances loading because of connection error: {}", e.getMessage());
            } else {
                log.error("Error while running process instances loading", e);
            }

            return 0;
        }
    }

    @Override
    public long getSuspendedProcessCount(BpmEngine bpmEngine) {
        ProcessInstanceApiClient processInstanceApiClient = processInstanceClientByEngineId
                .computeIfAbsent(bpmEngine.getId(), engineId -> createClient(bpmEngine, ProcessInstanceApiClient.class));
        try {
            ResponseEntity<CountResultDto> response = processInstanceApiClient.queryProcessInstancesCount(createProcessInstanceQueryDto()
                    .suspended(true));

            if (response.getStatusCode().is2xxSuccessful()) {
                return getCountResult(response.getBody());
            }
            log.error("Error while suspended process instances count loading, status code {}", response.getStatusCode());
            return 0;
        } catch (FeignException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Error while suspended process instances loading because of connection error: {}", e.getMessage());
            } else {
                log.error("Error while suspended process instances loading", e);
            }

            return 0;
        }
    }

    @Override
    public List<ProcessDefinitionStatistics> getProcessDefinitionStatistics(BpmEngine bpmEngine) {
        ProcessDefinitionApiClient processDefinitionApiClient = processDefinitionClientByEngineId
                .computeIfAbsent(bpmEngine.getId(), engineId -> createClient(bpmEngine, ProcessDefinitionApiClient.class));
        try {
            ResponseEntity<List<ProcessDefinitionStatisticsResultDto>> response = processDefinitionApiClient.getProcessDefinitionStatistics(true, null, null, true);
            String tenantId = engineTenantProvider.getCurrentUserTenantId();
            if (response.getStatusCode().is2xxSuccessful()) {
                List<ProcessDefinitionStatisticsResultDto> statisticsResultDtos = response.getBody();
                return CollectionUtils.emptyIfNull(statisticsResultDtos)
                        .stream()
                        .filter(processDefinitionStatisticsResultDto -> {
                            if (tenantId == null) {
                                return true;
                            }
                            ProcessDefinitionDto definition = processDefinitionStatisticsResultDto.getDefinition();
                            if (definition == null) {
                                return true;
                            }
                            return StringUtils.equals(definition.getTenantId(), tenantId);
                        })
                        .map(processDefinitionMapper::fromStatisticsResultDto)
                        .toList();
            }
            log.error("Error on process process definition statistics, status code {}", response.getStatusCode());
            return List.of();
        } catch (FeignException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Error while process definition statistics loading because of connection error: {}", e.getMessage());
            } else {
                log.error("Error while process definition statistics loading", e);
            }


            return List.of();
        }
    }

    protected List<ProcessExecutionGraphEntry> createWeeklyStatistics(OffsetDateTime from, OffsetDateTime to, List<HistoricProcessInstanceDto> startedInstances,
                                                                      List<HistoricProcessInstanceDto> finishedInstances) {
        Map<LocalDate, Long> startedInstancesByDate = startedInstances.stream()
                .collect(Collectors.groupingBy(historicInstance -> historicInstance.getStartTime().toLocalDate(), Collectors.counting()));

        Map<LocalDate, Long> finishedInstancesByDate = finishedInstances.stream()
                .filter(historicTask -> historicTask.getEndTime() != null)
                .collect(Collectors.groupingBy(historicInstance -> historicInstance.getEndTime().toLocalDate(), Collectors.counting()));

        LocalDate fromDate = from.toLocalDate();
        LocalDate toDate = to.toLocalDate().plusDays(1); //add a one day to include the last date of the period
        Stream<LocalDate> dates = fromDate.datesUntil(toDate);

        //generate data for each day in the period
        List<ProcessExecutionGraphEntry> items = dates.map(date -> {
                    ProcessExecutionGraphEntry item = metadata.create(ProcessExecutionGraphEntry.class);
                    item.setDate(date);
                    item.setStartedInstancesCount(startedInstancesByDate.getOrDefault(date, 0L));
                    item.setCompletedInstancesCount(finishedInstancesByDate.getOrDefault(date, 0L));
                    return item;
                })
                .toList();

        return items;
    }

    protected <V> V createClient(BpmEngine engine, Class<V> clientClass) {
        return feignClientProvider.createCamundaClient(new FeignClientCreationContext<>(clientClass)
                .setUrl(engine.getBaseUrl())
                .setRequestInterceptor(createBpmEngineRequestInterceptor(engine)));
    }

    @Nullable
    protected RequestInterceptor createBpmEngineRequestInterceptor(BpmEngine engine) {
        RequestInterceptor requestInterceptor = null;
        if (BooleanUtils.isTrue(engine.getAuthEnabled())) {
            if (engine.getAuthType() == AuthType.BASIC) {
                requestInterceptor = new BasicAuthRequestInterceptor(engine.getBasicAuthUsername(), engine.getBasicAuthPassword());
            } else if (engine.getAuthType() == AuthType.HTTP_HEADER) {
                requestInterceptor = requestTemplate -> {
                    requestTemplate.header(engine.getHttpHeaderName(), engine.getHttpHeaderValue());
                };
            }
        }
        return requestInterceptor;
    }

    protected List<HistoricProcessInstanceDto> loadStartedInstances(OffsetDateTime from, OffsetDateTime to, HistoryApiClient historyApiClient) {
        HistoricProcessInstanceQueryDto queryByStartedDate = createHistoricProcessInstanceQueryDto()
                .startedAfter(from)
                .startedBefore(to);
        try {
            ResponseEntity<List<HistoricProcessInstanceDto>> response = historyApiClient.queryHistoricProcessInstances(0, uiProperties.getRecentActivityMaxResults(), queryByStartedDate);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody()).orElse(List.of());
            }
            log.error("Unable to load started instances by period: from '{}', to '{}', status code: {}", from, to, response.getStatusCode().value());
            return List.of();
        } catch (FeignException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to load started instances by period: from '{}', to '{}' because of connection error: {}", from, to, e.getMessage());
            } else {
                log.error("Unable to load started instances by period: from '{}', to '{}'", from, to, e);
            }
            return List.of();
        }
    }

    protected List<HistoricProcessInstanceDto> loadFinishedInstances(OffsetDateTime from, OffsetDateTime to, HistoryApiClient historyApiClient) {
        HistoricProcessInstanceQueryDto queryByStartedDate = createHistoricProcessInstanceQueryDto()
                .finishedAfter(from)
                .finishedBefore(to);
        try {
            ResponseEntity<List<HistoricProcessInstanceDto>> response = historyApiClient.queryHistoricProcessInstances(0, uiProperties.getRecentActivityMaxResults(), queryByStartedDate);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody()).orElse(List.of());
            }
            log.error("Unable to load finished instances by period: from '{}', to '{}', status code: {}", from, to, response.getStatusCode().value());
            return List.of();
        } catch (FeignException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to load finished instances by period: from '{}', to '{}' because of connection error: {}", from, to, e.getMessage());
            } else {
                log.error("Unable to load finished instances by period: from '{}', to '{}'", from, to, e);
            }

            return List.of();
        }
    }

    @TransactionalEventListener
    public void onBpmEngineChangedAfterCommit(final EntityChangedEvent<BpmEngine> event) {
        Id<BpmEngine> entityId = event.getEntityId();
        if (event.getType() == EntityChangedEvent.Type.DELETED || event.getType() == EntityChangedEvent.Type.UPDATED) {
            processDefinitionClientByEngineId.remove((UUID) entityId.getValue());
            processInstanceClientByEngineId.remove((UUID) entityId.getValue());
            historyApiClientByEngineId.remove((UUID) entityId.getValue());
            taskClientByEngineId.remove((UUID) entityId.getValue());
        }
    }

    protected HistoricProcessInstanceQueryDto createHistoricProcessInstanceQueryDto() {
        HistoricProcessInstanceQueryDto historicProcessInstanceQueryDto = new HistoricProcessInstanceQueryDto();
        addTenant(historicProcessInstanceQueryDto, engineTenantProvider::getCurrentUserTenantId);
        return historicProcessInstanceQueryDto;
    }

    protected TaskQueryDto createTaskQueryDto() {
        TaskQueryDto taskQueryDto = new TaskQueryDto();

        addTenant(taskQueryDto, engineTenantProvider::getCurrentUserTenantId);
        return taskQueryDto;
    }


    protected ProcessInstanceQueryDto createProcessInstanceQueryDto() {
        ProcessInstanceQueryDto processInstanceQueryDto = new ProcessInstanceQueryDto();
        addTenant(processInstanceQueryDto, engineTenantProvider::getCurrentUserTenantId);
        return processInstanceQueryDto;
    }
}