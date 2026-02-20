/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.externaltask.impl;

import com.google.common.base.Strings;
import feign.utils.ExceptionUtils;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.EngineNotSelectedException;
import io.jmix.core.Sort;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.entity.filter.ExternalTaskFilter;
import io.flowset.control.mapper.ExternalTaskMapper;
import io.flowset.control.service.engine.EngineTenantProvider;
import io.flowset.control.service.externaltask.ExternalTaskLoadContext;
import io.flowset.control.service.externaltask.ExternalTaskService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.community.rest.client.api.ExternalTaskApiClient;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.impl.RemoteExternalTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.flowset.control.util.ExceptionUtils.isConnectionError;
import static io.flowset.control.util.QueryUtils.*;

@Service("control_ExternalTaskService")
@Slf4j
public class ExternalTaskServiceImpl implements ExternalTaskService {
    protected final RemoteExternalTaskService remoteExternalTaskService;
    protected final HistoryApiClient historyApiClient;
    protected final ExternalTaskApiClient externalTaskApiClient;
    protected final ExternalTaskMapper externalTaskMapper;

    private final EngineTenantProvider engineTenantProvider;

    public ExternalTaskServiceImpl(RemoteExternalTaskService remoteExternalTaskService,
                                   HistoryApiClient historyApiClient,
                                   ExternalTaskApiClient externalTaskApiClient,
                                   ExternalTaskMapper externalTaskMapper,
                                   EngineTenantProvider engineTenantProvider) {
        this.remoteExternalTaskService = remoteExternalTaskService;
        this.historyApiClient = historyApiClient;
        this.externalTaskApiClient = externalTaskApiClient;
        this.externalTaskMapper = externalTaskMapper;
        this.engineTenantProvider = engineTenantProvider;
    }

    @Override
    public List<ExternalTaskData> findRunningTasks(ExternalTaskLoadContext loadContext) {
        try {
            ExternalTaskQuery externalTaskQuery = createExternalTaskQuery();
            addSort(loadContext.getSort(), externalTaskQuery);
            addFilters(loadContext.getFilter(), externalTaskQuery);

            List<ExternalTask> externalTasks;
            if (loadContext.getFirstResult() != null && loadContext.getMaxResults() != null) {
                externalTasks = externalTaskQuery.listPage(loadContext.getFirstResult(), loadContext.getMaxResults());
            } else {
                externalTasks = externalTaskQuery.list();
            }
            return externalTasks
                    .stream()
                    .map(externalTaskMapper::fromExternalTask)
                    .toList();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load external tasks because BPM engine not selected");
                return List.of();
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load external tasks because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public long getRunningTasksCount(@Nullable ExternalTaskFilter filter) {
        try {
            ExternalTaskQuery externalTaskQuery = createExternalTaskQuery();
            addFilters(filter, externalTaskQuery);
            return externalTaskQuery.count();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to get running external tasks count because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void setRetries(String externalTaskId, int retries) {
        try {
            remoteExternalTaskService.setRetries(externalTaskId, retries);
            log.debug("Update retries count for external task {}. New value: {}", externalTaskId, retries);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to update retries for external task by id '{}' because of connection error: ", externalTaskId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void setRetriesAsync(List<String> externalTaskIds, int retries) {
        try {
            remoteExternalTaskService.setRetriesAsync(externalTaskIds, null, retries);
            log.debug("Async update retries count for external tasks {}. New value: {}", externalTaskIds, retries);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to update retries for external tasks because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public String getErrorDetails(String externalTaskId) {
        try {
            ResponseEntity<String> response = externalTaskApiClient.getExternalTaskErrorDetails(externalTaskId);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
            return "";
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable to get error details for task '{}' because of connection error: ", externalTaskId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    protected ExternalTaskQuery createExternalTaskQuery() {
        ExternalTaskQuery externalTaskQuery = remoteExternalTaskService.createExternalTaskQuery();
        String tenantId = engineTenantProvider.getCurrentUserTenantId();

        addIfNotNull(tenantId, externalTaskQuery::tenantIdIn);
        return externalTaskQuery;
    }

    protected void addFilters(@Nullable ExternalTaskFilter filter, ExternalTaskQuery externalTaskQuery) {
        if (filter != null) {
            addIfStringNotEmpty(filter.getProcessInstanceId(), externalTaskQuery::processInstanceId);
            addIfStringNotEmpty(filter.getActivityId(), externalTaskQuery::activityId);
        }
    }

    protected void addSort(Sort sort, ExternalTaskQuery externalTaskQuery) {
        if (sort != null) {
            for (Sort.Order order : sort.getOrders()) {
                String property = order.getProperty();
                boolean unknownValueUsed = false;
                switch (property) {
                    case "priority" -> externalTaskQuery.orderByPriority();
                    case "createTime" -> externalTaskQuery.orderByCreateTime();
                    case "id" -> externalTaskQuery.orderById();
                    default -> unknownValueUsed = true;
                }
                addSortDirection(externalTaskQuery, !unknownValueUsed, order);
            }

        }
    }
}