/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.usertask.impl;

import feign.utils.ExceptionUtils;
import io.jmix.core.Sort;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.filter.UserTaskFilter;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.mapper.TaskMapper;
import io.flowset.control.service.engine.EngineTenantProvider;
import io.flowset.control.service.usertask.UserTaskLoadContext;
import io.flowset.control.service.usertask.UserTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.api.TaskApiClient;
import org.camunda.community.rest.client.model.*;
import org.camunda.community.rest.impl.RemoteTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.flowset.control.service.variable.VariableUtils.createVariableMap;
import static io.flowset.control.util.EngineRestUtils.getCountResult;
import static io.flowset.control.util.QueryUtils.*;

@Service("control_UserTaskService")
@Slf4j
public class UserTaskServiceImpl implements UserTaskService {
    protected final RemoteTaskService remoteTaskService;
    protected final TaskMapper taskMapper;
    protected final HistoryApiClient historyApiClient;
    protected final TaskApiClient taskApiClient;
    protected final EngineTenantProvider tenantProvider;

    public UserTaskServiceImpl(RemoteTaskService remoteTaskService,
                               TaskMapper taskMapper,
                               HistoryApiClient historyApiClient,
                               TaskApiClient taskApiClient, EngineTenantProvider tenantProvider) {
        this.remoteTaskService = remoteTaskService;
        this.taskMapper = taskMapper;
        this.historyApiClient = historyApiClient;
        this.taskApiClient = taskApiClient;
        this.tenantProvider = tenantProvider;
    }


    @Override
    public List<UserTaskData> findRuntimeTasks(UserTaskLoadContext loadContext) {
        try {
            TaskQueryDto taskQueryDto = createTaskQueryDto(loadContext.getFilter());
            taskQueryDto.setSorting(createTaskQuerySort(loadContext.getSort()));

            ResponseEntity<List<TaskWithAttachmentAndCommentDto>> tasksResponse = taskApiClient.queryTasks(loadContext.getFirstResult(), loadContext.getMaxResults(), taskQueryDto);
            if (tasksResponse.getStatusCode().is2xxSuccessful()) {
                return CollectionUtils.emptyIfNull(tasksResponse.getBody())
                        .stream()
                        .map(taskMapper::fromRuntimeTaskDto)
                        .toList();
            }
            log.error("Error on user tasks loading, query: {}, status code: {}", taskQueryDto, tasksResponse.getStatusCode());
            return List.of();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load user tasks because BPM engine not selected");
                return List.of();
            }
            if (rootCause instanceof ConnectException) {
                log.error("Unable to load user tasks because of connection error: ", e);
                return List.of();
            }
            throw e;
        }
    }

    @Override
    public long getRuntimeTasksCount(@Nullable UserTaskFilter filter) {
        TaskQueryDto taskQueryDto = createTaskQueryDto(filter);
        ResponseEntity<CountResultDto> tasksResponse = taskApiClient.queryTasksCount(taskQueryDto);
        if (tasksResponse.getStatusCode().is2xxSuccessful()) {
            return getCountResult(tasksResponse.getBody());
        }
        log.error("Error on user task count loading, query: {}, status code: {}", taskQueryDto, tasksResponse.getStatusCode());
        return 0;
    }

    @Override
    public void completeTaskById(String taskId, Collection<VariableInstanceData> variableInstances) {
        VariableMap variables = createVariableMap(variableInstances);
        remoteTaskService.complete(taskId, variables);
    }

    @Override
    @Nullable
    public UserTaskData findTaskById(String taskId) {
        ResponseEntity<List<HistoricTaskInstanceDto>> taskResponse = historyApiClient.queryHistoricTaskInstances(0, 1, new HistoricTaskInstanceQueryDto()
                .taskId(taskId));
        if (taskResponse.getStatusCode().is2xxSuccessful()) {
            List<HistoricTaskInstanceDto> taskInstanceDtoList = taskResponse.getBody();

            if (CollectionUtils.isNotEmpty(taskInstanceDtoList)) {
                HistoricTaskInstanceDto task = taskInstanceDtoList.get(0);
                return taskMapper.fromTaskDto(task);
            }

            return null;
        }
        log.error("Error on user task loading, task id: {}, status code: {}", taskId, taskResponse.getStatusCode());
        return null;
    }

    @Override
    public List<UserTaskData> findHistoricTasks(UserTaskLoadContext loadContext) {
        HistoricTaskInstanceQueryDto queryDto = createHistoryTaskQueryDto(loadContext.getFilter());

        queryDto.setSorting(createHistoryTaskSortOptions(loadContext.getSort()));
        ResponseEntity<List<HistoricTaskInstanceDto>> response = historyApiClient.queryHistoricTaskInstances(loadContext.getFirstResult(), loadContext.getMaxResults(),
                queryDto);

        if (response.getStatusCode().is2xxSuccessful()) {
            List<HistoricTaskInstanceDto> taskInstanceDtoList = response.getBody();
            return CollectionUtils.emptyIfNull(taskInstanceDtoList)
                    .stream()
                    .map(taskMapper::fromTaskDto)
                    .toList();

        }
        log.error("Error on historic user task loading, query: {}, status code: {}", queryDto, response.getStatusCode());
        return List.of();
    }

    @Override
    public long getHistoryTasksCount(@Nullable UserTaskFilter filter) {
        HistoricTaskInstanceQueryDto queryDto = createHistoryTaskQueryDto(filter);
        ResponseEntity<CountResultDto> response = historyApiClient.queryHistoricTaskInstancesCount(queryDto);

        if (response.getStatusCode().is2xxSuccessful()) {
            return getCountResult(response.getBody());
        }
        log.error("Error on historic user task count loading, query: {}, status code: {}", queryDto, response.getStatusCode());
        return 0;
    }

    @Override
    public void setAssignee(String taskId, String newAssignee) {
        remoteTaskService.setAssignee(taskId, newAssignee);
    }


    protected TaskQueryDto createTaskQueryDto(@Nullable UserTaskFilter filter) {
        TaskQueryDto taskQueryDto = createTaskQueryDto();
        if (filter != null) {
            addIfStringNotEmpty(filter.getProcessInstanceId(), taskQueryDto::setProcessInstanceId);
            addIfStringNotEmpty(filter.getActivityInstanceId(), value -> taskQueryDto.setActivityInstanceIdIn(List.of(value)));
            addIfStringNotEmpty(filter.getProcessDefinitionKey(), taskQueryDto::setProcessDefinitionKey);

            wrapAndAddStringIfNotEmpty(filter.getAssigneeLike(), taskQueryDto::setAssigneeLike);
            wrapAndAddStringIfNotEmpty(filter.getTaskKeyLike(), taskQueryDto::setTaskDefinitionKeyLike);
            wrapAndAddStringIfNotEmpty(filter.getTaskNameLike(), taskQueryDto::setNameLike);

            addIfNotNull(filter.getCreatedAfter(), taskQueryDto::createdAfter);
            addIfNotNull(filter.getCreatedBefore(), taskQueryDto::createdBefore);

            taskQueryDto.setAssigned(filter.getAssigned());
            taskQueryDto.setUnassigned(filter.getUnassigned());

            taskQueryDto.setActive(filter.getActive());
            taskQueryDto.setSuspended(filter.getSuspended());
        }

        return taskQueryDto;
    }

    protected TaskQueryDto createTaskQueryDto() {
        TaskQueryDto taskQueryDto = new TaskQueryDto();
        addTenant(taskQueryDto, tenantProvider::getCurrentUserTenantId);
        return taskQueryDto;
    }

    protected List<TaskQueryDtoSortingInner> createTaskQuerySort(@Nullable Sort sort) {
        if (sort == null) {
            return null;
        }
        List<TaskQueryDtoSortingInner> taskSortOptions = new ArrayList<>();
        TaskQueryDtoSortingInner sortOption = new TaskQueryDtoSortingInner();
        for (Sort.Order order : sort.getOrders()) {
            switch (order.getProperty()) {
                case "id" -> sortOption.setSortBy(TaskQueryDtoSortingInner.SortByEnum.ID);
                case "name" -> sortOption.setSortBy(TaskQueryDtoSortingInner.SortByEnum.NAME_CASE_INSENSITIVE);
                case "createTime" -> sortOption.setSortBy(TaskQueryDtoSortingInner.SortByEnum.CREATED);
                case "dueDate" -> sortOption.setSortBy(TaskQueryDtoSortingInner.SortByEnum.DUE_DATE);
                case "assignee" -> sortOption.setSortBy(TaskQueryDtoSortingInner.SortByEnum.ASSIGNEE);
                default -> {
                }
            }

            if (order.getDirection() == Sort.Direction.ASC) {
                sortOption.setSortOrder(TaskQueryDtoSortingInner.SortOrderEnum.ASC);
            } else if (order.getDirection() == Sort.Direction.DESC) {
                sortOption.setSortOrder(TaskQueryDtoSortingInner.SortOrderEnum.DESC);
            }
        }

        if (sortOption.getSortBy() != null && sortOption.getSortOrder() != null) {
            taskSortOptions.add(sortOption);
        }
        return taskSortOptions;
    }

    protected HistoricTaskInstanceQueryDto createHistoryTaskQueryDto(@Nullable UserTaskFilter filter) {
        HistoricTaskInstanceQueryDto taskQueryDto = new HistoricTaskInstanceQueryDto();
        if (filter != null) {
            addIfStringNotEmpty(filter.getProcessInstanceId(), taskQueryDto::setProcessInstanceId);
            addIfStringNotEmpty(filter.getActivityInstanceId(), value -> taskQueryDto.setActivityInstanceIdIn(List.of(value)));
        }

        return taskQueryDto;
    }

    protected List<HistoricTaskInstanceQueryDtoSortingInner> createHistoryTaskSortOptions(Sort sort) {
        List<HistoricTaskInstanceQueryDtoSortingInner> taskSortOptions = new ArrayList<>();
        if (sort != null) {
            HistoricTaskInstanceQueryDtoSortingInner sortOption = new HistoricTaskInstanceQueryDtoSortingInner();
            for (Sort.Order order : sort.getOrders()) {
                switch (order.getProperty()) {
                    case "id" -> sortOption.setSortBy(HistoricTaskInstanceQueryDtoSortingInner.SortByEnum.TASK_ID);
                    case "taskDefinitionKey" ->
                            sortOption.setSortBy(HistoricTaskInstanceQueryDtoSortingInner.SortByEnum.TASK_DEFINITION_KEY);
                    case "name" -> sortOption.setSortBy(HistoricTaskInstanceQueryDtoSortingInner.SortByEnum.TASK_NAME);
                    case "startTime" ->
                            sortOption.setSortBy(HistoricTaskInstanceQueryDtoSortingInner.SortByEnum.START_TIME);
                    case "endTime" ->
                            sortOption.setSortBy(HistoricTaskInstanceQueryDtoSortingInner.SortByEnum.END_TIME);
                    case "dueDate" ->
                            sortOption.setSortBy(HistoricTaskInstanceQueryDtoSortingInner.SortByEnum.DUE_DATE);
                    case "assignee" ->
                            sortOption.setSortBy(HistoricTaskInstanceQueryDtoSortingInner.SortByEnum.ASSIGNEE);
                    default -> {
                    }
                }

                if (order.getDirection() == Sort.Direction.ASC) {
                    sortOption.setSortOrder(HistoricTaskInstanceQueryDtoSortingInner.SortOrderEnum.ASC);
                } else if (order.getDirection() == Sort.Direction.DESC) {
                    sortOption.setSortOrder(HistoricTaskInstanceQueryDtoSortingInner.SortOrderEnum.DESC);
                }
            }

            if (sortOption.getSortBy() != null && sortOption.getSortOrder() != null) {
                taskSortOptions.add(sortOption);
            }
        }
        return taskSortOptions;
    }
}
