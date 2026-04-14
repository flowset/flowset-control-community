/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.batch.impl;

import feign.FeignException;
import feign.utils.ExceptionUtils;
import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.mapper.BatchMapper;
import io.flowset.control.service.batch.BatchLoadContext;
import io.flowset.control.service.batch.BatchService;
import io.flowset.control.service.engine.EngineTenantProvider;
import io.jmix.core.Sort;
import io.jmix.core.entity.EntityValues;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.community.rest.client.api.BatchApiClient;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.model.BatchDto;
import org.camunda.community.rest.client.model.BatchStatisticsDto;
import org.camunda.community.rest.client.model.CountResultDto;
import org.camunda.community.rest.client.model.HistoricBatchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.flowset.control.util.EngineRestUtils.getCountResult;
import static io.flowset.control.util.ExceptionUtils.isConnectionError;

@Service("control_BatchService")
@Slf4j
@AllArgsConstructor
public class BatchServiceImpl implements BatchService {

    protected final BatchApiClient batchApiClient;
    protected final HistoryApiClient historyApiClient;
    protected final BatchMapper batchMapper;
    protected final EngineTenantProvider engineTenantProvider;

    @Override
    public List<BatchStatisticsData> findAllBatchStatistics(BatchLoadContext loadContext) {
        try {
            BatchFilter filter = loadContext.getFilter();
            String sortBy = getBatchStatisticsSortProperty(loadContext.getSort());
            String sortOrder = sortBy != null ? getSortOrder(loadContext.getSort()) : null;
            String tenantIdIn = getTenantIdIn();
            ResponseEntity<List<BatchStatisticsDto>> response = batchApiClient.getBatchStatistics(
                    sortBy,
                    sortOrder,
                    loadContext.getFirstResult(),
                    loadContext.getMaxResults(),
                    getFilterPropertyValue(filter, "batchId"),
                    getFilterPropertyValue(filter, "type"),
                    tenantIdIn,
                    tenantIdIn == null,
                    getFilterPropertyValue(filter, "suspended"),
                    getFilterPropertyValue(filter, "createdBy"),
                    getFilterPropertyValue(filter, "startedBefore"),
                    getFilterPropertyValue(filter, "startedAfter"),
                    getFilterPropertyValue(filter, "withFailures"),
                    getFilterPropertyValue(filter, "withoutFailures")
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return CollectionUtils.emptyIfNull(response.getBody())
                        .stream()
                        .map(batchMapper::fromBatchStatisticsDto)
                        .toList();
            }

            log.error("Error on loading batch statistics, status code {}", response.getStatusCode());
            return List.of();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load batch statistics because BPM engine not selected");
                return List.of();
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load batch statistics because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    @Nullable
    public BatchStatisticsData findBatchStatisticsById(String batchId) {
        try {
            String tenantIdIn = getTenantIdIn();

            ResponseEntity<List<BatchStatisticsDto>> response = batchApiClient.getBatchStatistics(
                    null,
                    null,
                    0,
                    1,
                    batchId,
                    null,
                    tenantIdIn,
                    tenantIdIn == null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return CollectionUtils.emptyIfNull(response.getBody())
                        .stream()
                        .findFirst()
                        .map(batchMapper::fromBatchStatisticsDto)
                        .orElse(null);
            }
            log.error("Error on loading batch statistics by id {}, status code {}", batchId, response.getStatusCode());
            return null;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load batch statistics by id '{}' because BPM engine not selected", batchId);
                return null;
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load batch statistics by id '{}' because of connection error: ", batchId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    @Nullable
    public BatchData getById(String batchId) {
        try {
            BatchData runtimeBatch = findRuntimeBatchById(batchId);
            if (runtimeBatch != null) {
                return runtimeBatch;
            }
            return findHistoricBatchById(batchId);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load batch by id '{}' because BPM engine not selected", batchId);
                return null;
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load batch by id '{}' because of connection error: ", batchId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }


    @Override
    @Nullable
    public BatchData findHistoricBatchById(String batchId) {
        try {
            ResponseEntity<List<HistoricBatchDto>> response = historyApiClient.getHistoricBatches(
                    batchId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    1
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return CollectionUtils.emptyIfNull(response.getBody())
                        .stream()
                        .findFirst()
                        .map(batchMapper::fromHistoricBatchDto)
                        .orElse(null);
            }
            log.error("Error on loading historic batch by id {}, status code {}", batchId, response.getStatusCode());
            return null;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load historic batch by id '{}' because BPM engine not selected", batchId);
                return null;
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load historic batch by id '{}' because of connection error: ", batchId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public long getBatchStatisticsCount(@Nullable BatchFilter filter) {
        try {
            String tenantIdIn = getTenantIdIn();
            ResponseEntity<CountResultDto> response = batchApiClient.getBatchStatisticsCount(
                    getFilterPropertyValue(filter, "batchId"),
                    getFilterPropertyValue(filter, "type"),
                    tenantIdIn,
                    tenantIdIn == null,
                    getFilterPropertyValue(filter, "suspended"),
                    getFilterPropertyValue(filter, "createdBy"),
                    getFilterPropertyValue(filter, "startedBefore"),
                    getFilterPropertyValue(filter, "startedAfter"),
                    getFilterPropertyValue(filter, "withFailures"),
                    getFilterPropertyValue(filter, "withoutFailures")
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return getCountResult(response.getBody());
            }

            log.error("Error on loading batch statistics count, status code {}", response.getStatusCode());
            return 0;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load batch statistics count because BPM engine not selected");
                return 0;
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load batch statistics count because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public List<BatchData> findAllHistoricBatches(BatchLoadContext loadContext) {
        try {
            BatchFilter filter = loadContext.getFilter();
            String sortBy = getHistoricBatchSortProperty(loadContext.getSort());
            String sortOrder = sortBy != null ? getSortOrder(loadContext.getSort()) : null;

            String tenantIdIn = getTenantIdIn();

            ResponseEntity<List<HistoricBatchDto>> response = historyApiClient.getHistoricBatches(
                    getFilterPropertyValue(filter, "batchId"),
                    getFilterPropertyValue(filter, "type"),
                    getFilterPropertyValue(filter, "completed"),
                    tenantIdIn,
                    tenantIdIn == null,
                    sortBy,
                    sortOrder,
                    loadContext.getFirstResult(),
                    loadContext.getMaxResults()
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return CollectionUtils.emptyIfNull(response.getBody())
                        .stream()
                        .map(batchMapper::fromHistoricBatchDto)
                        .toList();
            }

            log.error("Error on loading historic batches, status code {}", response.getStatusCode());
            return List.of();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load historic batches because BPM engine not selected");
                return List.of();
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load historic batches because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public long getHistoricBatchCount(@Nullable BatchFilter filter) {
        try {
            String tenantIdIn = getTenantIdIn();
            ResponseEntity<CountResultDto> response = historyApiClient.getHistoricBatchesCount(
                    getFilterPropertyValue(filter, "batchId"),
                    getFilterPropertyValue(filter, "type"),
                    getFilterPropertyValue(filter, "completed"),
                    tenantIdIn,
                    tenantIdIn == null
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return getCountResult(response.getBody());
            }

            log.error("Error on loading historic batch count, status code {}", response.getStatusCode());
            return 0;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load historic batch count because BPM engine not selected");
                return 0;
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load historic batch count because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }


    @Nullable
    protected BatchData findRuntimeBatchById(String batchId) {
        try {
            ResponseEntity<BatchDto> runtimeResponse = batchApiClient.getBatch(batchId);
            if (runtimeResponse.getStatusCode().is2xxSuccessful()) {
                BatchDto body = runtimeResponse.getBody();
                return body != null ? batchMapper.fromBatchDto(body) : null;
            }
            log.error("Error on loading runtime batch by id {}, status code {}", batchId, runtimeResponse.getStatusCode());
            return null;
        } catch (FeignException.NotFound e) {
            log.debug("Runtime batch by id '{}' was not found, trying historic batch", batchId);
            return null;
        }
    }

    @Nullable
    protected String getBatchStatisticsSortProperty(@Nullable Sort sort) {
        if (sort == null || CollectionUtils.isEmpty(sort.getOrders())) {
            return null;
        }

        Sort.Order order = sort.getOrders().get(0);

        return switch (order.getProperty()) {
            case "id" -> "batchId";
            case "startTime" -> "startTime";
            case "type" -> "type";
            default -> null;
        };
    }

    @Nullable
    protected String getHistoricBatchSortProperty(@Nullable Sort sort) {
        if (sort == null || CollectionUtils.isEmpty(sort.getOrders())) {
            return null;
        }

        Sort.Order order = sort.getOrders().get(0);

        return switch (order.getProperty()) {
            case "id" -> "batchId";
            case "startTime" -> "startTime";
            case "type" -> "type";
            case "endTime" -> "endTime";
            default -> null;
        };
    }

    @Nullable
    protected String getSortOrder(@Nullable Sort sort) {
        if (sort == null || CollectionUtils.isEmpty(sort.getOrders())) {
            return null;
        }

        Sort.Direction direction = sort.getOrders().get(0).getDirection();
        return direction.name().toLowerCase();
    }

    @Nullable
    protected String getTenantIdIn() {
        return engineTenantProvider.getCurrentUserTenantId();
    }

    @Nullable
    protected <V> V getFilterPropertyValue(Object filter, String propertyName) {
        return filter != null ? EntityValues.getValue(filter, propertyName) : null;
    }

}
