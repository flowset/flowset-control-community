/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.incident.impl;

import feign.utils.ExceptionUtils;
import io.jmix.core.Messages;
import io.jmix.core.Sort;
import io.flowset.control.dto.ActivityIncidentData;
import io.flowset.control.entity.filter.IncidentFilter;
import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.mapper.IncidentMapper;
import io.flowset.control.service.engine.EngineTenantProvider;
import io.flowset.control.service.incident.IncidentLoadContext;
import io.flowset.control.service.incident.IncidentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.api.IncidentApiClient;
import org.camunda.community.rest.client.model.CountResultDto;
import org.camunda.community.rest.client.model.HistoricIncidentDto;
import org.camunda.community.rest.client.model.IncidentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.flowset.control.util.EngineRestUtils.getCountResult;

@Service("control_IncidentService")
@Slf4j
public class IncidentServiceImpl implements IncidentService {
    protected final IncidentApiClient incidentApiClient;
    protected final HistoryApiClient historyApiClient;
    protected final IncidentMapper incidentMapper;
    protected final Messages messages;
    protected final EngineTenantProvider engineTenantProvider;

    public IncidentServiceImpl(IncidentApiClient incidentApiClient,
                               HistoryApiClient historyApiClient,
                               IncidentMapper incidentMapper, Messages messages, EngineTenantProvider engineTenantProvider) {
        this.incidentApiClient = incidentApiClient;
        this.historyApiClient = historyApiClient;
        this.incidentMapper = incidentMapper;
        this.messages = messages;
        this.engineTenantProvider = engineTenantProvider;
    }

    @Override
    public List<ActivityIncidentData> findRuntimeIncidents(String processInstanceId) {
        ResponseEntity<List<IncidentDto>> response = incidentApiClient.getIncidents(null, null, null,
                null, null, null, processInstanceId, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null);

        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            List<IncidentDto> incidents = Optional.ofNullable(response.getBody()).orElse(List.of());
            return incidents.stream()
                    .collect(Collectors.groupingBy(IncidentDto::getActivityId, Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> {
                        int incidentCount = entry.getValue().intValue();
                        return new ActivityIncidentData(entry.getKey(), incidentCount);
                    })
                    .toList();
        }
        log.error("Error on loading incidents by process instance {}, status code {}", processInstanceId, response.getStatusCode());

        return List.of();
    }

    @Override
    public List<IncidentData> findRuntimeIncidents(IncidentLoadContext loadContext) {
        try {
            String tenantId = engineTenantProvider.getCurrentUserTenantId();
            String sortBy = getRuntimeIncidentSortProperty(loadContext.getSort());
            String sortOrder = null;
            if (sortBy != null) {
                sortOrder = getIncidentSortOrder(loadContext.getSort());
            }
            IncidentFilter filter = loadContext.getFilter();

            ResponseEntity<List<IncidentDto>> response = incidentApiClient.getIncidents(
                    getIncidentId(filter), getIncidentType(filter), null, getIncidentMessageLike(filter), getProcessDefinitionId(filter),
                    getProcessDefinitionKey(filter), getProcessInstanceId(filter), null, getTimestampBefore(filter),
                    getTimestampAfter(filter), getActivityId(filter),
                    null, null, null, null, tenantId, null,
                    sortBy, sortOrder, loadContext.getFirstResult(), loadContext.getMaxResults()
            );

            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                List<IncidentDto> incidents = Optional.ofNullable(response.getBody()).orElse(List.of());

                return incidents
                        .stream()
                        .map(incidentMapper::fromIncidentModel)
                        .toList();
            }
            log.error("Error on incidents loading, status code {}", response.getStatusCode());

            return List.of();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load runtime incidents because BPM engine not selected");
                return List.of();
            }
            if (rootCause instanceof ConnectException) {
                log.error("Unable to load runtime incidents because of connection error: ", e);
                return List.of();
            }
            throw e;
        }
    }

    @Override
    public IncidentData findRuntimeIncidentById(String incidentId) {
        try {
            ResponseEntity<IncidentDto> response = incidentApiClient.getIncident(incidentId);
            if (response.getStatusCode().is2xxSuccessful()) {
                IncidentDto incidentDto = response.getBody();
                return incidentDto != null ? incidentMapper.fromIncidentModel(incidentDto) : null;
            }

            log.error("Error on loading incident by id {}, status code {}", incidentId, response.getStatusCode());
            return null;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load runtime incident by id {} because BPM engine not selected", incidentId);
                return null;
            }
            if (rootCause instanceof ConnectException) {
                log.error("Unable to load runtime incident by id '{}' because of connection error: ", incidentId, e);
                return null;
            }
            throw e;
        }
    }

    @Override
    public long getRuntimeIncidentCount(IncidentFilter filter) {
        String tenantId = engineTenantProvider.getCurrentUserTenantId();
        ResponseEntity<CountResultDto> response = incidentApiClient.getIncidentsCount(
                getIncidentId(filter), getIncidentType(filter), null, getIncidentMessageLike(filter), getProcessDefinitionId(filter),
                getProcessDefinitionKey(filter), getProcessInstanceId(filter), null, getTimestampBefore(filter),
                getTimestampAfter(filter), getActivityId(filter),
                null, null, null, null, tenantId, null
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return getCountResult(response.getBody());
        }
        log.error("Error on incident count loading, status code {}", response.getStatusCode());
        return 0;
    }

    @Override
    public List<HistoricIncidentData> findHistoricIncidents(IncidentLoadContext loadContext) {
        String tenantId = engineTenantProvider.getCurrentUserTenantId();
        String sortBy = getHistoryIncidentSortProperty(loadContext.getSort());
        String sortOrder = null;
        if (sortBy != null) {
            sortOrder = getIncidentSortOrder(loadContext.getSort());
        }
        IncidentFilter filter = loadContext.getFilter();

        ResponseEntity<List<HistoricIncidentDto>> response = historyApiClient.getHistoricIncidents(
                getIncidentId(filter), getIncidentType(filter), null, getIncidentMessageLike(filter), getProcessDefinitionId(filter),
                getProcessDefinitionKey(filter), null, getProcessInstanceId(filter), null, null,
                null, null, null, getActivityId(filter),
                null, null, null, null, null, null, null, null, tenantId, null, null,
                sortBy, sortOrder, loadContext.getFirstResult(), loadContext.getMaxResults()
        );

        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            List<HistoricIncidentDto> incidents = Optional.ofNullable(response.getBody()).orElse(List.of());

            return incidents
                    .stream()
                    .map(incidentMapper::fromHistoricIncidentModel)
                    .toList();
        }
        return List.of();
    }


    @Override
    public long getHistoricIncidentCount(@Nullable IncidentFilter filter) {
        String tenantId = engineTenantProvider.getCurrentUserTenantId();
        ResponseEntity<CountResultDto> response = historyApiClient.getHistoricIncidentsCount(
                getIncidentId(filter), getIncidentType(filter), null, getIncidentMessageLike(filter), getProcessDefinitionId(filter),
                getProcessDefinitionKey(filter), null, getProcessInstanceId(filter), null, null,
                null, null, null, getActivityId(filter),
                null, null, null, null, null, null, null, null, tenantId, null, null
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return getCountResult(response.getBody());
        }
        log.error("Error on historic incident count loading, status code {}", response.getStatusCode());
        return 0;
    }

    @Override
    @Nullable
    public HistoricIncidentData findHistoricIncidentById(String id) {
        String tenantId = engineTenantProvider.getCurrentUserTenantId();
        ResponseEntity<List<HistoricIncidentDto>> response = historyApiClient.getHistoricIncidents(id, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null,
                null, null, null, null, null, null, null, null, tenantId, null, null,
                null, null, 0, 1);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<HistoricIncidentDto> historicIncidentDtos = response.getBody();

            if (CollectionUtils.isNotEmpty(historicIncidentDtos)) {
                HistoricIncidentDto incidentDto = historicIncidentDtos.get(0);
                return incidentMapper.fromHistoricIncidentModel(incidentDto);
            } else {
                return null;
            }
        }

        log.error("Error on loading historic incident by id {}, status code {}", id, response.getStatusCode());
        return null;
    }

    protected String getRuntimeIncidentSortProperty(@Nullable Sort sort) {
        if (sort == null || sort.getOrders().isEmpty()) {
            return null;
        }

        Sort.Order order = sort.getOrders().get(0);

        return switch (order.getProperty()) {
            case "id" -> "incidentId";
            case "type" -> "incidentType";
            case "timestamp" -> "incidentTimestamp";
            case "message" -> "incidentMessage";
            case "processInstanceId" -> "processInstanceId";
            case "processDefinitionId" -> "processDefinitionId";
            case "activityId" -> "activityId";
            default -> null;
        };
    }

    protected String getHistoryIncidentSortProperty(@Nullable Sort sort) {
        if (sort == null || sort.getOrders().isEmpty()) {
            return null;
        }

        Sort.Order order = sort.getOrders().get(0);

        return switch (order.getProperty()) {
            case "id" -> "incidentId";
            case "type" -> "incidentType";
            case "createTime" -> "createTime";
            case "endTime" -> "endTime";
            case "message" -> "incidentMessage";
            case "activityId" -> "activityId";
            case "resolved" -> "incidentState";
            default -> null;
        };
    }

    protected String getIncidentSortOrder(@Nullable Sort sort) {
        if (sort == null || sort.getOrders().isEmpty()) {
            return null;
        }

        Sort.Order order = sort.getOrders().get(0);
        Sort.Direction direction = order.getDirection();
        if (direction == Sort.Direction.ASC) {
            return "asc";
        } else if (direction == Sort.Direction.DESC) {
            return "desc";
        }

        return null;
    }

    @Nullable
    protected String getProcessInstanceId(@Nullable IncidentFilter filter) {
        return filter != null ? filter.getProcessInstanceId() : null;
    }

    @Nullable
    protected String getIncidentId(@Nullable IncidentFilter filter) {
        return filter != null ? filter.getIncidentId() : null;
    }

    @Nullable
    protected String getIncidentType(@Nullable IncidentFilter filter) {
        return filter != null ? filter.getIncidentType() : null;
    }

    @Nullable
    protected String getProcessDefinitionId(@Nullable IncidentFilter filter) {
        return filter != null ? filter.getProcessDefinitionId() : null;
    }

    @Nullable
    protected String getProcessDefinitionKey(@Nullable IncidentFilter filter) {
        return filter != null ? filter.getProcessDefinitionKey() : null;
    }

    @Nullable
    protected String getIncidentMessageLike(@Nullable IncidentFilter filter) {
        return filter != null && filter.getIncidentMessageLike() != null ? "%" + filter.getIncidentMessageLike() + "%" : null;
    }

    @Nullable
    protected String getActivityId(IncidentFilter filter) {
        return filter != null ? filter.getActivityId() : null;
    }

    @Nullable
    protected OffsetDateTime getTimestampBefore(IncidentFilter filter) {
        return filter != null ? filter.getIncidentTimestampBefore() : null;
    }

    @Nullable
    protected OffsetDateTime getTimestampAfter(IncidentFilter filter) {
        return filter != null ? filter.getIncidentTimestampAfter() : null;
    }
}