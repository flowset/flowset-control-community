/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable.impl;

import feign.utils.ExceptionUtils;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.jmix.core.EntityStates;
import io.jmix.core.Sort;
import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.entity.variable.ObjectTypeInfo;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.mapper.VariableMapper;
import io.flowset.control.service.client.EngineRestClient;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.EngineTenantProvider;
import io.flowset.control.service.variable.VariableLoadContext;
import io.flowset.control.service.variable.VariableService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.api.ProcessInstanceApiClient;
import org.camunda.community.rest.client.api.VariableInstanceApiClient;
import org.camunda.community.rest.client.model.*;
import org.camunda.community.rest.impl.RemoteRuntimeService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

import static io.flowset.control.util.EngineRestUtils.getCountResult;
import static io.flowset.control.util.ExceptionUtils.isConnectionError;

@Service("control_VariableService")
@Slf4j
public class VariableServiceImpl implements VariableService {

    protected final HistoryApiClient historyApiClient;
    protected final VariableMapper variableMapper;
    protected final RemoteRuntimeService remoteRuntimeService;
    protected final VariableInstanceApiClient variableInstanceApiClient;
    protected final ProcessInstanceApiClient processInstanceApiClient;
    protected final EntityStates entityStates;
    protected final EngineService engineService;
    protected final EngineRestClient engineRestClient;
    protected final EngineTenantProvider engineTenantProvider;

    public VariableServiceImpl(HistoryApiClient historyApiClient,
                               VariableMapper variableMapper,
                               RemoteRuntimeService remoteRuntimeService,
                               VariableInstanceApiClient variableInstanceApiClient,
                               ProcessInstanceApiClient processInstanceApiClient,
                               EntityStates entityStates,
                               EngineService engineService,
                               EngineRestClient engineRestClient, EngineTenantProvider engineTenantProvider) {
        this.historyApiClient = historyApiClient;
        this.variableMapper = variableMapper;
        this.remoteRuntimeService = remoteRuntimeService;
        this.variableInstanceApiClient = variableInstanceApiClient;
        this.processInstanceApiClient = processInstanceApiClient;
        this.entityStates = entityStates;
        this.engineService = engineService;
        this.engineRestClient = engineRestClient;
        this.engineTenantProvider = engineTenantProvider;
    }

    @Override
    public List<VariableInstanceData> findRuntimeVariables(VariableLoadContext loadContext) {
        VariableInstanceQueryDto queryDto = createVariableInstanceQuery(loadContext.getFilter());

        Sort sort = loadContext.getSort();
        if (sort != null) {
            List<VariableInstanceQueryDtoSortingInner> sortDtoList = createSortDtoList(sort);
            queryDto.setSorting(sortDtoList);
        }

        ResponseEntity<List<VariableInstanceDto>> response = variableInstanceApiClient.queryVariableInstances(loadContext.getFirstResult(), loadContext.getMaxResults(),
                false, queryDto
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return CollectionUtils.emptyIfNull(response.getBody())
                    .stream()
                    .map(variableInstanceDto -> {
                        VariableInstanceData variableInstanceData = variableMapper.fromVariableDto(variableInstanceDto);
                        entityStates.setNew(variableInstanceData, false);
                        return variableInstanceData;
                    })
                    .toList();
        }

        log.error("Error on loading runtime variables, query {}, status code {}", queryDto, response.getStatusCode());
        return List.of();
    }

    @Override
    public VariableInstanceData findRuntimeVariableById(String variableInstanceId) {
        try {
            ResponseEntity<VariableInstanceDto> response = variableInstanceApiClient.getVariableInstance(variableInstanceId, false);
            if (response.getStatusCode().is2xxSuccessful()) {
                VariableInstanceDto variableInstanceDto = response.getBody();
                return variableInstanceDto != null ? variableMapper.fromVariableDto(variableInstanceDto) : null;
            }
            log.error("Error on loading runtime variables, variable id {}, status code {}", variableInstanceId, response.getStatusCode());
            return null;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable get runtime variables by id '{}' because of connection error: ", variableInstanceId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }

            throw e;
        }
    }

    @Override
    public Resource getVariableInstanceBinary(String variableInstanceId) {
        ResponseEntity<Resource> response = variableInstanceApiClient.getVariableInstanceBinary(variableInstanceId);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        return null;
    }

    @Override
    public List<HistoricVariableInstanceData> findHistoricVariables(VariableLoadContext loadContext) {
        HistoricVariableInstanceQueryDto queryDto = createHistoricVariableQuery(loadContext.getFilter());

        Sort sort = loadContext.getSort();
        if (sort != null) {
            List<HistoricVariableInstanceQueryDtoSortingInner> sortDtoList = createHistoricVariableSortOptions(sort);
            queryDto.setSorting(sortDtoList);
        }

        ResponseEntity<List<HistoricVariableInstanceDto>> response = historyApiClient.queryHistoricVariableInstances(loadContext.getFirstResult(), loadContext.getMaxResults(), true, queryDto);

        if (response.getStatusCode().is2xxSuccessful()) {
            return CollectionUtils.emptyIfNull(response.getBody())
                    .stream()
                    .map(variableMapper::fromHistoricVariableInstanceDto)
                    .toList();
        }
        log.error("Error on loading historic variables, query {}, status code {}", queryDto, response.getStatusCode());
        return List.of();
    }


    @Override
    public long getRuntimeVariablesCount(@Nullable VariableFilter filter) {
        try {
            VariableInstanceQueryDto queryDto = createVariableInstanceQuery(filter);
            ResponseEntity<CountResultDto> response = variableInstanceApiClient.queryVariableInstancesCount(queryDto);
            if (response.getStatusCode().is2xxSuccessful()) {
                return getCountResult(response.getBody());
            }

            log.error("Error on loading runtime variables count, query {}, status code {}", queryDto, response.getStatusCode());
            return 0;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable get runtime variables count because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }

            throw e;
        }
    }

    @Override
    public long getHistoricVariablesCount(@Nullable VariableFilter filter) {
        try {
            HistoricVariableInstanceQueryDto queryDto = createHistoricVariableQuery(filter);
            ResponseEntity<CountResultDto> response = historyApiClient.queryHistoricVariableInstancesCount(queryDto);
            if (response.getStatusCode().is2xxSuccessful()) {
                return getCountResult(response.getBody());
            }
            log.error("Error on loading historic variables count, query {}, status code {}", queryDto, response.getStatusCode());
            return 0;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable get historic variables count because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }

            throw e;
        }
    }

    @Override
    public void updateVariableLocal(VariableInstanceData variableInstanceData) {
        Objects.requireNonNull(variableInstanceData.getExecutionId(), "executionId can not be null");

        try {
            VariableValueDto variableValueDto = new VariableValueDto();
            variableValueDto.type(variableInstanceData.getType());

            if (variableInstanceData.getValueInfo() != null && variableInstanceData.getValueInfo().getObject() != null) {
                ObjectTypeInfo objectTypeInfo = variableInstanceData.getValueInfo().getObject();
                Map<String, Object> valueInfoMap = new HashMap<>();
                valueInfoMap.put("objectTypeName", objectTypeInfo.getObjectTypeName());
                valueInfoMap.put("serializationDataFormat", objectTypeInfo.getSerializationDataFormat());

                variableValueDto.valueInfo(valueInfoMap);
            }
            variableValueDto.value(variableInstanceData.getValue());

            processInstanceApiClient.setProcessInstanceVariable(variableInstanceData.getExecutionId(), variableInstanceData.getName(), variableValueDto);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable update local variable {} because of connection error: ", variableInstanceData.getName(), e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }

            throw e;
        }
    }

    @Override
    public HistoricVariableInstanceData findHistoricVariableById(String variableInstanceId) {
        ResponseEntity<HistoricVariableInstanceDto> response = historyApiClient.getHistoricVariableInstance(variableInstanceId, true);
        if (response.getStatusCode().is2xxSuccessful()) {
            HistoricVariableInstanceDto variableInstanceDto = response.getBody();
            return variableInstanceDto != null ? variableMapper.fromHistoricVariableInstanceDto(variableInstanceDto) : null;
        }
        log.error("Error on find historic variable, variable id {}, status code {}", variableInstanceId, response.getStatusCode());
        return null;
    }

    @Override
    public void removeVariableLocal(VariableInstanceData variableInstanceData) {
        Objects.requireNonNull(variableInstanceData.getExecutionId(), "executionId can not be null");
        remoteRuntimeService.removeVariableLocal(variableInstanceData.getExecutionId(), variableInstanceData.getName());
    }

    @Override
    public void removeVariablesLocal(String executionId, Set<VariableInstanceData> variableItems) {
        Objects.requireNonNull(executionId, "executionId can not be null");

        try {
            List<String> nameList = variableItems.stream()
                    .map(VariableInstanceData::getName)
                    .toList();

            remoteRuntimeService.removeVariablesLocal(executionId, nameList);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable remove local variables for execution {} because of connection error: ", executionId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }

            throw e;
        }
    }

    @Override
    public void updateVariableBinary(VariableInstanceData variableInstanceData, File data) {
        engineRestClient.updateVariableBinary(variableInstanceData, data);
    }

    protected List<VariableInstanceQueryDtoSortingInner> createSortDtoList(Sort sort) {
        List<VariableInstanceQueryDtoSortingInner> sortDtoList = new ArrayList<>();
        for (Sort.Order order : sort.getOrders()) {
            String property = order.getProperty();

            VariableInstanceQueryDtoSortingInner sortDto = new VariableInstanceQueryDtoSortingInner();
            switch (property) {
                case "name" -> sortDto.setSortBy(VariableInstanceQueryDtoSortingInner.SortByEnum.VARIABLE_NAME);
                case "activityInstanceId" ->
                        sortDto.setSortBy(VariableInstanceQueryDtoSortingInner.SortByEnum.ACTIVITY_INSTANCE_ID);
                case "type" -> sortDto.setSortBy(VariableInstanceQueryDtoSortingInner.SortByEnum.VARIABLE_TYPE);
                default -> {
                }
            }

            if (order.getDirection() == Sort.Direction.ASC) {
                sortDto.setSortOrder(VariableInstanceQueryDtoSortingInner.SortOrderEnum.ASC);
            } else if (order.getDirection() == Sort.Direction.DESC) {
                sortDto.setSortOrder(VariableInstanceQueryDtoSortingInner.SortOrderEnum.DESC);
            }

            if (sortDto.getSortBy() != null && sortDto.getSortOrder() != null) {
                sortDtoList.add(sortDto);
            }
        }
        return sortDtoList;
    }

    protected HistoricVariableInstanceQueryDto createHistoricVariableQuery(@Nullable VariableFilter filter) {
        HistoricVariableInstanceQueryDto queryDto = new HistoricVariableInstanceQueryDto();

        if (filter != null) {
            if (StringUtils.isNotBlank(filter.getActivityInstanceId())) {
                queryDto.activityInstanceIdIn(List.of(filter.getActivityInstanceId()));
            }
            if (StringUtils.isNotBlank(filter.getProcessInstanceId())) {
                queryDto.processInstanceId(filter.getProcessInstanceId());
            }
        }

        return queryDto;
    }


    protected List<HistoricVariableInstanceQueryDtoSortingInner> createHistoricVariableSortOptions(Sort sort) {
        List<HistoricVariableInstanceQueryDtoSortingInner> sortDtoList = new ArrayList<>();
        for (Sort.Order order : sort.getOrders()) {
            String property = order.getProperty();

            HistoricVariableInstanceQueryDtoSortingInner sortDto = new HistoricVariableInstanceQueryDtoSortingInner();
            if (property.equals("name")) {
                sortDto.setSortBy(HistoricVariableInstanceQueryDtoSortingInner.SortByEnum.VARIABLE_NAME);
            }

            if (order.getDirection() == Sort.Direction.ASC) {
                sortDto.setSortOrder(HistoricVariableInstanceQueryDtoSortingInner.SortOrderEnum.ASC);
            } else if (order.getDirection() == Sort.Direction.DESC) {
                sortDto.setSortOrder(HistoricVariableInstanceQueryDtoSortingInner.SortOrderEnum.DESC);
            }

            if (sortDto.getSortBy() != null && sortDto.getSortOrder() != null) {
                sortDtoList.add(sortDto);
            }
        }
        return sortDtoList;
    }

    protected VariableInstanceQueryDto createVariableInstanceQuery(@Nullable VariableFilter filter) {
        VariableInstanceQueryDto variableInstanceQueryDto = new VariableInstanceQueryDto();
        String tenantId = engineTenantProvider.getCurrentUserTenantId();
        if (tenantId != null) {
            variableInstanceQueryDto.tenantIdIn(List.of(tenantId));
        }

        if (filter != null) {
            if (StringUtils.isNotBlank(filter.getActivityInstanceId())) {
                variableInstanceQueryDto.activityInstanceIdIn(List.of(filter.getActivityInstanceId()));
            }
            if (StringUtils.isNotBlank(filter.getProcessInstanceId())) {
                variableInstanceQueryDto.addProcessInstanceIdInItem(filter.getProcessInstanceId());
            }
            if (StringUtils.isNotBlank(filter.getVariableName())) {
                variableInstanceQueryDto.setVariableName(filter.getVariableName());
            }
        }
        return variableInstanceQueryDto;
    }
}
