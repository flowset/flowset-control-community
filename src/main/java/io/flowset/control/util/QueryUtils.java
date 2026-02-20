/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.util;

import io.jmix.core.Sort;
import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.entity.filter.DecisionDefinitionFilter;
import io.flowset.control.entity.filter.DecisionInstanceFilter;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.filter.ProcessInstanceFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionState;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.community.rest.client.model.HistoricProcessInstanceQueryDto;
import org.camunda.community.rest.client.model.HistoricProcessInstanceQueryDtoSortingInner;
import org.camunda.community.rest.client.model.ProcessInstanceQueryDto;
import org.camunda.community.rest.client.model.TaskQueryDto;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class QueryUtils {

    public static final String INSTANCE_ID = "id";
    public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
    public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    public static final String BUSINESS_KEY = "businessKey";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";

    public static void addRuntimeFilters(ProcessInstanceQuery processInstanceQuery, @Nullable ProcessInstanceFilter filter) {
        if (filter == null) {
            return;
        }

        addIfNotNull(filter.getProcessInstanceIds(), instanceIds -> processInstanceQuery.processInstanceIds(new HashSet<>(instanceIds)));
        addIfStringNotEmpty(filter.getProcessDefinitionId(), processInstanceQuery::processDefinitionId);
        addIfStringNotEmpty(filter.getProcessDefinitionKey(), processInstanceQuery::processDefinitionKey);
        wrapAndAddStringIfNotEmpty(filter.getBusinessKeyLike(), processInstanceQuery::processInstanceBusinessKeyLike);

        addIfTrue(filter.getActive(), processInstanceQuery::active);
        addIfTrue(filter.getSuspended(), processInstanceQuery::suspended);
        addIfTrue(filter.getWithIncidents(), processInstanceQuery::withIncident);

        addIfNotNull(filter.getActiveActivityIdIn(), activityList -> processInstanceQuery.activityIdIn(activityList.toArray(new String[0])));
    }

    public static void addRuntimeSort(ProcessInstanceQuery processInstanceQuery, @Nullable Sort sort) {
        if (sort != null) {
            for (Sort.Order order : sort.getOrders()) {
                boolean unknownValueUsed = false;
                switch (order.getProperty()) {
                    case INSTANCE_ID -> processInstanceQuery.orderByProcessInstanceId();
                    case PROCESS_DEFINITION_ID -> processInstanceQuery.orderByProcessDefinitionId();
                    case PROCESS_DEFINITION_KEY -> processInstanceQuery.orderByProcessDefinitionKey();
                    case BUSINESS_KEY -> processInstanceQuery.orderByBusinessKey();
                    default -> unknownValueUsed = true;
                }

                addSortDirection(processInstanceQuery, !unknownValueUsed, order);
            }
        }
    }

    public static void addHistoryFilters(HistoricProcessInstanceQueryDto queryDto, @Nullable ProcessInstanceFilter filter) {
        if (filter == null) {
            return;
        }

        addIfNotNull(filter.getProcessInstanceIds(), queryDto::processInstanceIds);
        addIfStringNotEmpty(filter.getProcessDefinitionId(), queryDto::processDefinitionId);
        addIfStringNotEmpty(filter.getProcessDefinitionKey(), queryDto::processDefinitionKey);
        addIfStringNotEmpty(filter.getProcessInstanceId(), queryDto::processInstanceId);
        wrapAndAddStringIfNotEmpty(filter.getBusinessKeyLike(), queryDto::processInstanceBusinessKeyLike);


        addIfTrue(filter.getFinished(), () -> queryDto.setFinished(true));
        addIfTrue(filter.getActive(), () -> queryDto.setActive(true));
        addIfTrue(filter.getSuspended(), () -> queryDto.setSuspended(true));
        addIfTrue(filter.getUnfinished(), () -> queryDto.setUnfinished(true));
        addIfTrue(filter.getWithIncidents(), () -> {
            queryDto.withIncidents(true);
            queryDto.incidentStatus(HistoricProcessInstanceQueryDto.IncidentStatusEnum.OPEN);
        });

        addIfNotNull(filter.getStartTimeAfter(), queryDto::startedAfter);
        addIfNotNull(filter.getStartTimeBefore(), queryDto::startedBefore);
        addIfNotNull(filter.getEndTimeAfter(), queryDto::finishedAfter);
        addIfNotNull(filter.getEndTimeBefore(), queryDto::finishedBefore);
        addIfNotNull(filter.getActiveActivityIdIn(), queryDto::activeActivityIdIn);
    }

    public static void addHistorySort(HistoricProcessInstanceQueryDto queryDto, @Nullable Sort sort) {
        if (sort != null) {
            List<HistoricProcessInstanceQueryDtoSortingInner> sortOptions = new ArrayList<>();
            for (Sort.Order order : sort.getOrders()) {
                HistoricProcessInstanceQueryDtoSortingInner.SortByEnum sortBy = switch (order.getProperty()) {
                    case INSTANCE_ID -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.INSTANCE_ID;
                    case PROCESS_DEFINITION_ID -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITION_ID;
                    case PROCESS_DEFINITION_KEY -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.DEFINITION_KEY;
                    case BUSINESS_KEY -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.BUSINESS_KEY;
                    case START_TIME -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.START_TIME;
                    case END_TIME -> HistoricProcessInstanceQueryDtoSortingInner.SortByEnum.END_TIME;
                    default -> null;
                };

                HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum sortOrder = null;
                Sort.Direction direction = order.getDirection();
                if (direction == Sort.Direction.ASC) {
                    sortOrder = HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.ASC;
                } else if (direction == Sort.Direction.DESC) {
                    sortOrder = HistoricProcessInstanceQueryDtoSortingInner.SortOrderEnum.DESC;
                }

                if (sortBy != null && sortOrder != null) {
                    sortOptions.add(new HistoricProcessInstanceQueryDtoSortingInner()
                            .sortBy(sortBy)
                            .sortOrder(sortOrder));
                }
            }
            queryDto.setSorting(sortOptions);
        }
    }

    public static void addDefinitionFilters(ProcessDefinitionQuery processDefinitionQuery, @Nullable ProcessDefinitionFilter filter) {
        if (filter == null) {
            return;
        }

        wrapAndAddStringIfNotEmpty(filter.getKeyLike(), processDefinitionQuery::processDefinitionKeyLike);
        wrapAndAddStringIfNotEmpty(filter.getNameLike(), processDefinitionQuery::processDefinitionNameLike);

        addIfStringNotEmpty(filter.getKey(), processDefinitionQuery::processDefinitionKey);
        addIfStringNotEmpty(filter.getVersionTag(), processDefinitionQuery::versionTag);
        addIfNotNull(filter.getVersion(), processDefinitionQuery::processDefinitionVersion);

        addCollectionIfNotEmpty(filter.getKeyIn(), processDefinitionQuery::processDefinitionKeyIn);
        addCollectionIfNotEmpty(filter.getIdIn(), processDefinitionQuery::processDefinitionIdIn);

        addIfTrue(filter.getLatestVersionOnly(), processDefinitionQuery::latestVersion);

        addIfStringNotEmpty(filter.getDeploymentId(), processDefinitionQuery::deploymentId);

        addIfTrue(filter.getState() == ProcessDefinitionState.ACTIVE, processDefinitionQuery::active);
        addIfTrue(filter.getState() == ProcessDefinitionState.SUSPENDED, processDefinitionQuery::suspended);
    }

    public static void addDeploymentFilters(DeploymentQuery deploymentQuery, @Nullable DeploymentFilter filter) {
        if (filter == null) {
            return;
        }

        addIfStringNotEmpty(filter.getDeploymentId(), deploymentQuery::deploymentId);

        wrapAndAddStringIfNotEmpty(filter.getNameLike(), deploymentQuery::deploymentNameLike);

        addIfStringNotEmpty(filter.getSource(), deploymentQuery::deploymentSource);

        addCollectionIfNotEmpty(filter.getTenantIdIn(), deploymentQuery::tenantIdIn);
        addIfTrue(filter.getWithoutTenantId(), deploymentQuery::withoutTenantId);

        if (filter.getDeploymentAfter() != null) {
            deploymentQuery.deploymentAfter(Date.from(filter.getDeploymentAfter().toInstant()));
        }
        if (filter.getDeploymentBefore() != null) {
            deploymentQuery.deploymentBefore(Date.from(filter.getDeploymentBefore().toInstant()));
        }
    }

    public static void addDecisionDefinitionFilters(DecisionDefinitionQuery decisionDefinitionQuery,
                                                    @Nullable DecisionDefinitionFilter filter) {
        if (filter == null) {
            return;
        }
        wrapAndAddStringIfNotEmpty(filter.getKeyLike(), decisionDefinitionQuery::decisionDefinitionKeyLike);
        wrapAndAddStringIfNotEmpty(filter.getNameLike(), decisionDefinitionQuery::decisionDefinitionNameLike);
        addIfStringNotEmpty(filter.getKey(), decisionDefinitionQuery::decisionDefinitionKey);
        addCollectionIfNotEmpty(filter.getIdIn(), decisionDefinitionQuery::decisionDefinitionIdIn);
        addIfNotNull(filter.getVersion(), decisionDefinitionQuery::decisionDefinitionVersion);
        addIfTrue(filter.getLatestVersionOnly(), decisionDefinitionQuery::latestVersion);
        addIfNotNull(filter.getVersionTag(), decisionDefinitionQuery::versionTag);
        addIfNotNull(filter.getDeploymentId(), decisionDefinitionQuery::deploymentId);
    }

    public static void addDecisionInstanceFilters(HistoricDecisionInstanceQuery decisionInstanceQuery,
                                                  @Nullable DecisionInstanceFilter filter) {
        if (filter == null) {
            return;
        }
        addIfStringNotEmpty(filter.getDecisionInstanceId(), decisionInstanceQuery::decisionInstanceId);
        addIfStringNotEmpty(filter.getDecisionDefinitionKey(), decisionInstanceQuery::decisionDefinitionKey);
        addIfStringNotEmpty(filter.getDecisionDefinitionId(), decisionInstanceQuery::decisionDefinitionId);
        addIfStringNotEmpty(filter.getProcessDefinitionKey(), decisionInstanceQuery::processDefinitionKey);
        addIfStringNotEmpty(filter.getProcessInstanceId(), decisionInstanceQuery::processInstanceId);
        addIfStringNotEmpty(filter.getActivityId(), decisionInstanceQuery::activityIdIn);
        if (filter.getEvaluatedAfter() != null) {
            decisionInstanceQuery.evaluatedAfter(Date.from(filter.getEvaluatedAfter().toInstant()));
        }
        if (filter.getEvaluatedBefore() != null) {
            decisionInstanceQuery.evaluatedBefore(Date.from(filter.getEvaluatedBefore().toInstant()));
        }
    }

    public static void addDefinitionSort(ProcessDefinitionQuery processDefinitionQuery, @Nullable Sort sort) {
        if (sort != null) {
            for (Sort.Order order : sort.getOrders()) {
                boolean unknownValueUsed = false;
                switch (order.getProperty()) {
                    case "name" -> processDefinitionQuery.orderByProcessDefinitionName();
                    case "key" -> processDefinitionQuery.orderByProcessDefinitionKey();
                    case "version" -> processDefinitionQuery.orderByProcessDefinitionVersion();
                    default -> unknownValueUsed = true;
                }

                addSortDirection(processDefinitionQuery, !unknownValueUsed, order);
            }
        }
    }

    public static void addDeploymentSort(DeploymentQuery deploymentQuery, @Nullable Sort sort) {
        if (sort != null) {
            for (Sort.Order order : sort.getOrders()) {
                boolean unknownValueUsed = false;
                switch (order.getProperty()) {
                    case "id" -> deploymentQuery.orderByDeploymentId();
                    case "name" -> deploymentQuery.orderByDeploymentName();
                    case "time" -> deploymentQuery.orderByDeploymentTime();
                    case "tenantId" -> deploymentQuery.orderByTenantId();
                    default -> unknownValueUsed = true;
                }

                addSortDirection(deploymentQuery, !unknownValueUsed, order);
            }
        }
    }

    public static void addDecisionDefinitionSort(DecisionDefinitionQuery processDefinitionQuery, @Nullable Sort sort) {
        if (sort != null) {
            for (Sort.Order order : sort.getOrders()) {
                boolean unknownValueUsed = false;
                switch (order.getProperty()) {
                    case "name" -> processDefinitionQuery.orderByDecisionDefinitionName();
                    case "key" -> processDefinitionQuery.orderByDecisionDefinitionKey();
                    case "version" -> processDefinitionQuery.orderByDecisionDefinitionVersion();
                    default -> unknownValueUsed = true;
                }
                addSortDirection(processDefinitionQuery, !unknownValueUsed, order);
            }
        }
    }

    public static void addDecisionInstanceSort(HistoricDecisionInstanceQuery decisionInstanceQuery,
                                               @Nullable Sort sort) {
        if (sort != null) {
            for (Sort.Order order : sort.getOrders()) {
                boolean unknownValueUsed = false;
                switch (order.getProperty()) {
                    case "evaluationTime" -> decisionInstanceQuery.orderByEvaluationTime();
                    case "tenantId" -> decisionInstanceQuery.orderByEvaluationTime();
                    default -> unknownValueUsed = true;
                }
                addSortDirection(decisionInstanceQuery, !unknownValueUsed, order);
            }
        }
    }

    public static void addIfStringNotEmpty(String filterValue, Consumer<String> filterValueConsumer) {
        if (StringUtils.hasText(filterValue)) {
            filterValueConsumer.accept(filterValue);
        }
    }

    public static void wrapAndAddStringIfNotEmpty(String filterValue, Consumer<String> filterValueConsumer) {
        if (StringUtils.hasText(filterValue)) {
            filterValueConsumer.accept("%" + filterValue + "%");
        }
    }

    public static void addIfTrue(Boolean filterValue, Runnable filterValueConsumer) {
        if (BooleanUtils.isTrue(filterValue)) {
            filterValueConsumer.run();
        }
    }

    public static <V> void addIfNotNull(V filterValue, Consumer<V> filterValueConsumer) {
        if (filterValue != null) {
            filterValueConsumer.accept(filterValue);
        }
    }

    public static void addCollectionIfNotEmpty(Collection<String> filterValues, Consumer<String> filterValueConsumer) {
        if (CollectionUtils.isNotEmpty(filterValues)) {
            filterValueConsumer.accept(String.join(",", filterValues));
        }
    }

    public static void addSortDirection(Query<?, ?> query, boolean propertyIsSet, Sort.Order order) {
        if (!propertyIsSet) {
            return;
        }
        Sort.Direction direction = order.getDirection();
        if (direction == Sort.Direction.ASC) {
            query.asc();
        } else if (direction == Sort.Direction.DESC) {
            query.desc();
        }
    }

    public static void addTenant(ProcessDefinitionQuery processDefinitionQuery, Supplier<String> tenantIdSupplier) {
        String tenantId = tenantIdSupplier.get();
        addIfNotNull(tenantId, processDefinitionQuery::tenantIdIn);
    }

    public static void addTenant(ProcessInstanceQueryDto processInstanceQuery, Supplier<String> tenantIdSupplier) {
        String tenantId = tenantIdSupplier.get();
        addSingleListValueIfNotEmpty(tenantId, processInstanceQuery::tenantIdIn);
    }

    public static void addTenant(TaskQueryDto taskQueryDto, Supplier<String> tenantIdSupplier) {
        String tenantId = tenantIdSupplier.get();

        addSingleListValueIfNotEmpty(tenantId, taskQueryDto::tenantIdIn);
    }
    public static void addTenant(HistoricProcessInstanceQueryDto historicTaskInstanceQueryDto, Supplier<String> tenantIdSupplier) {
        String tenantId = tenantIdSupplier.get();
        addSingleListValueIfNotEmpty(tenantId, historicTaskInstanceQueryDto::tenantIdIn);
    }

    public static void addSingleListValueIfNotEmpty(String filterValue, Consumer<List<String>> filterValueConsumer) {
        if (StringUtils.hasText(filterValue)) {
            filterValueConsumer.accept(List.of(filterValue));
        }
    }

}
