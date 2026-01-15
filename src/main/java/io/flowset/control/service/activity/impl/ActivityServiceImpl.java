/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.activity.impl;

import feign.FeignException;
import feign.utils.ExceptionUtils;
import io.jmix.core.Sort;
import io.flowset.control.entity.activity.ActivityInstanceTreeItem;
import io.flowset.control.entity.activity.ActivityShortData;
import io.flowset.control.entity.activity.HistoricActivityInstanceData;
import io.flowset.control.entity.activity.ProcessActivityStatistics;
import io.flowset.control.entity.filter.ActivityFilter;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.mapper.ActivityMapper;
import io.flowset.control.mapper.HistoryActivityMapper;
import io.flowset.control.service.activity.ActivityLoadContext;
import io.flowset.control.service.activity.ActivityService;
import io.flowset.control.service.engine.EngineTenantProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient;
import org.camunda.community.rest.client.api.ProcessInstanceApiClient;
import org.camunda.community.rest.client.model.ActivityInstanceDto;
import org.camunda.community.rest.client.model.ActivityStatisticsResultDto;
import org.camunda.community.rest.client.model.CountResultDto;
import org.camunda.community.rest.client.model.HistoricActivityInstanceDto;
import org.camunda.community.rest.client.model.HistoricActivityInstanceQueryDto;
import org.camunda.community.rest.client.model.HistoricActivityInstanceQueryDtoSortingInner;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.flowset.control.util.EngineRestUtils.getCountResult;
import static io.flowset.control.util.ExceptionUtils.isConnectionError;

@Service("control_ActivityService")
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    protected final HistoryActivityMapper historyActivityMapper;
    protected final HistoryApiClient historyApiClient;
    protected final ActivityMapper activityMapper;
    protected final ProcessInstanceApiClient processInstanceApi;
    protected final EngineTenantProvider engineTenantProvider;
    protected final ProcessDefinitionApiClient processDefinitionApi;


    public ActivityServiceImpl(HistoryActivityMapper historyActivityMapper,
                               HistoryApiClient historyApiClient,
                               ActivityMapper activityMapper,
                               ProcessInstanceApiClient processInstanceApi,
                               EngineTenantProvider engineTenantProvider,
                               ProcessDefinitionApiClient processDefinitionApi) {
        this.historyActivityMapper = historyActivityMapper;
        this.historyApiClient = historyApiClient;
        this.activityMapper = activityMapper;
        this.processInstanceApi = processInstanceApi;
        this.processDefinitionApi = processDefinitionApi;
        this.engineTenantProvider = engineTenantProvider;
    }

    @Override
    public List<ActivityShortData> findRunningActivities(String processInstanceId) {
        HistoricActivityInstanceQueryDto queryDto = createHistoricActivityInstanceQueryDto()
                .processInstanceId(processInstanceId)
                .unfinished(true);

        ResponseEntity<List<HistoricActivityInstanceDto>> response = historyApiClient.queryHistoricActivityInstances(null, null,
                queryDto);
        if (response.getStatusCode().is2xxSuccessful()) {
            List<HistoricActivityInstanceDto> activityInstanceDtoList = response.getBody();
            return CollectionUtils.emptyIfNull(activityInstanceDtoList)
                    .stream()
                    .map(activityMapper::fromActivityDto)
                    .toList();
        }

        log.error("Error while getting running history activities by process instance id {},  response code {}", processInstanceId, response.getStatusCode());
        return List.of();
    }

    @Override
    public List<ActivityInstanceTreeItem> getActivityInstancesTree(String processInstanceId) {
        List<ActivityInstanceTreeItem> items = new ArrayList<>();
        ResponseEntity<ActivityInstanceDto> response = processInstanceApi.getActivityInstanceTree(processInstanceId);
        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            ActivityInstanceDto rootActivityInstance = response.getBody();
            ActivityInstanceTreeItem rootTreeItem = cresteActivityInstanceTreeItem(null, rootActivityInstance);
            items.add(rootTreeItem);

            addChildItems(rootActivityInstance, rootTreeItem, items);
            addTransitionItems(rootActivityInstance, rootTreeItem, items);
        } else {
            log.error("Error while getting activity instances tree by process instance id {},  response code {}", processInstanceId, response.getStatusCode());
        }

        return items;
    }


    @Override
    public List<ActivityShortData> findFinishedActivities(String processInstanceId) {
        HistoricActivityInstanceQueryDto queryDto = createHistoricActivityInstanceQueryDto()
                .processInstanceId(processInstanceId)
                .finished(true);

        ResponseEntity<List<HistoricActivityInstanceDto>> response = historyApiClient.queryHistoricActivityInstances(null, null,
                queryDto);
        if (response.getStatusCode().is2xxSuccessful()) {
            List<HistoricActivityInstanceDto> historicActivityInstanceDtos = response.getBody();
            return CollectionUtils.emptyIfNull(historicActivityInstanceDtos)
                    .stream()
                    .map(activityMapper::fromActivityDto)
                    .toList();
        }
        log.error("Error while getting finished history activities by process instance id {}, response code {}", processInstanceId, response.getStatusCode());
        return List.of();
    }

    @Override
    public List<HistoricActivityInstanceData> findAllHistoryActivities(ActivityLoadContext loadContext) {
        HistoricActivityInstanceQueryDto queryDto = createActivityQueryDto(loadContext.getFilter());
        addSortOptions(loadContext.getSort(), queryDto);

        ResponseEntity<List<HistoricActivityInstanceDto>> response = historyApiClient.queryHistoricActivityInstances(loadContext.getFirstResult(), loadContext.getMaxResults(),
                queryDto);
        if (response.getStatusCode().is2xxSuccessful()) {
            List<HistoricActivityInstanceDto> activityInstanceDtoList = response.getBody();
            return CollectionUtils.emptyIfNull(activityInstanceDtoList)
                    .stream()
                    .map(historyActivityMapper::fromHistoryActivityDto)
                    .toList();
        }

        log.error("Error while getting history activities, {}", response.getStatusCode());
        return List.of();
    }

    @Override
    public long getHistoryActivitiesCount(@Nullable ActivityFilter filter) {
        HistoricActivityInstanceQueryDto queryDto = createActivityQueryDto(filter);
        ResponseEntity<CountResultDto> response = historyApiClient.queryHistoricActivityInstancesCount(queryDto);
        if (response.getStatusCode().is2xxSuccessful()) {
            return getCountResult(response.getBody());
        }

        log.error("Error while getting history activities count, {}", response.getStatusCode());
        return 0;
    }

    @Override
    @Nullable
    public HistoricActivityInstanceData findById(String activityInstanceId) {
        ResponseEntity<HistoricActivityInstanceDto> response = historyApiClient.getHistoricActivityInstance(activityInstanceId);
        if (response.getStatusCode().is2xxSuccessful()) {
            HistoricActivityInstanceDto activityInstanceDto = response.getBody();
            return activityInstanceDto != null ? historyActivityMapper.fromHistoryActivityDto(activityInstanceDto) : null;
        }
        log.error("Error while getting history activity by id {}, status code: {}", activityInstanceId, response.getStatusCode());
        return null;
    }

    @Override
    public List<ProcessActivityStatistics> getStatisticsByProcessId(String processDefinitionId) {
        try {
            ResponseEntity<List<ActivityStatisticsResultDto>> response = processDefinitionApi.getActivityStatistics(processDefinitionId, true, true, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                List<ActivityStatisticsResultDto> statisticsResultDtoList = response.getBody();
                if (statisticsResultDtoList != null) {
                    return statisticsResultDtoList
                            .stream()
                            .map(activityMapper::fromActivityStatisticsResult)
                            .toList();
                }
            }
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load process definition activity statistics by id '{}' because BPM engine not selected", processDefinitionId);
                return null;
            }

            if (isConnectionError(rootCause)) {
                log.error("Unable load process definition activity statistics by id '{}' because of connection error: ", processDefinitionId, e);
                return null;
            }

            if (rootCause instanceof FeignException feignException && feignException.status() == 404) {
                log.warn("Unable to load process definition activity statistics by id '{}' because process does not exist", processDefinitionId);
                return null;
            }

            throw e;

        }
        return List.of();
    }

    protected HistoricActivityInstanceQueryDto createActivityQueryDto(@Nullable ActivityFilter filter) {
        HistoricActivityInstanceQueryDto queryDto = createHistoricActivityInstanceQueryDto();

        if (filter != null) {
            queryDto.setProcessInstanceId(filter.getProcessInstanceId());
        }

        return queryDto;
    }

    protected HistoricActivityInstanceQueryDto createHistoricActivityInstanceQueryDto() {
        HistoricActivityInstanceQueryDto historicActivityInstanceQueryDto = new HistoricActivityInstanceQueryDto();
        String currentUserTenantId = engineTenantProvider.getCurrentUserTenantId();
        if (currentUserTenantId != null) {
            historicActivityInstanceQueryDto.addTenantIdInItem(currentUserTenantId);
        }
        return historicActivityInstanceQueryDto;
    }

    protected void addSortOptions(Sort sort, HistoricActivityInstanceQueryDto queryDto) {
        if (sort != null) {
            List<HistoricActivityInstanceQueryDtoSortingInner> sortOptions = new ArrayList<>();
            for (Sort.Order order : sort.getOrders()) {
                HistoricActivityInstanceQueryDtoSortingInner sortOption = new HistoricActivityInstanceQueryDtoSortingInner();
                switch (order.getProperty()) {
                    case "activityId" ->
                            sortOption.setSortBy(HistoricActivityInstanceQueryDtoSortingInner.SortByEnum.ACTIVITY_ID);
                    case "activityType" ->
                            sortOption.setSortBy(HistoricActivityInstanceQueryDtoSortingInner.SortByEnum.ACTIVITY_TYPE);
                    case "activityName" ->
                            sortOption.setSortBy(HistoricActivityInstanceQueryDtoSortingInner.SortByEnum.ACTIVITY_NAME);
                    case "startTime" ->
                            sortOption.setSortBy(HistoricActivityInstanceQueryDtoSortingInner.SortByEnum.START_TIME);
                    case "endTime" ->
                            sortOption.setSortBy(HistoricActivityInstanceQueryDtoSortingInner.SortByEnum.END_TIME);
                    default -> {
                    }
                }

                if (order.getDirection() == Sort.Direction.ASC) {
                    sortOption.setSortOrder(HistoricActivityInstanceQueryDtoSortingInner.SortOrderEnum.ASC);
                } else if (order.getDirection() == Sort.Direction.DESC) {
                    sortOption.setSortOrder(HistoricActivityInstanceQueryDtoSortingInner.SortOrderEnum.DESC);
                }

                if (sortOption.getSortBy() != null && sortOption.getSortOrder() != null) {
                    sortOptions.add(sortOption);
                }
            }

            queryDto.setSorting(sortOptions);
        }
    }

    protected void addChildItems(ActivityInstanceDto rootItem, ActivityInstanceTreeItem parentItem, List<ActivityInstanceTreeItem> resultItems) {
        if (rootItem == null || CollectionUtils.isEmpty(rootItem.getChildActivityInstances())) {
            return;
        }

        rootItem.getChildActivityInstances()
                .forEach(activityInstanceDto -> {
                    ActivityInstanceTreeItem activityInstanceTreeItem = cresteActivityInstanceTreeItem(parentItem, activityInstanceDto);
                    resultItems.add(activityInstanceTreeItem);

                    addChildItems(activityInstanceDto, activityInstanceTreeItem, resultItems);
                    addTransitionItems(activityInstanceDto, activityInstanceTreeItem, resultItems);
                });
    }

    protected ActivityInstanceTreeItem cresteActivityInstanceTreeItem(@Nullable ActivityInstanceTreeItem treeItem, ActivityInstanceDto activityInstanceDto) {
        ActivityInstanceTreeItem activityInstanceTreeItem = activityMapper.fromRuntimeActivityDto(activityInstanceDto);
        activityInstanceTreeItem.setParentActivityInstance(treeItem);
        return activityInstanceTreeItem;
    }

    protected void addTransitionItems(ActivityInstanceDto rootItem, ActivityInstanceTreeItem treeItem, List<ActivityInstanceTreeItem> resultItems) {
        if (rootItem == null || CollectionUtils.isEmpty(rootItem.getChildTransitionInstances())) {
            return;
        }

        rootItem.getChildTransitionInstances()
                .forEach(transitionInstanceDto -> {
                    ActivityInstanceTreeItem activityInstanceTreeItem = activityMapper.fromRuntimeTransitionDto(transitionInstanceDto);
                    activityInstanceTreeItem.setParentActivityInstance(treeItem);
                    resultItems.add(activityInstanceTreeItem);
                });
    }
}
