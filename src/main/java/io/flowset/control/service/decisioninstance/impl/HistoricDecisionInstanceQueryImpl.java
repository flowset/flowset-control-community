package io.flowset.control.service.decisioninstance.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.model.CountResultDto;
import org.camunda.community.rest.client.model.HistoricDecisionInputInstanceDto;
import org.camunda.community.rest.client.model.HistoricDecisionInstanceDto;
import org.camunda.community.rest.client.model.HistoricDecisionOutputInstanceDto;
import org.camunda.community.rest.impl.query.BaseQuery;
import org.camunda.community.rest.impl.query.QueryOrderingProperty;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static io.flowset.control.util.EngineRestUtils.getCountResult;

@Slf4j
public class HistoricDecisionInstanceQueryImpl extends BaseQuery<HistoricDecisionInstanceQuery, HistoricDecisionInstance>
        implements HistoricDecisionInstanceQuery {

    private final HistoryApiClient historyApiClient;

    private String decisionInstanceId;
    private String decisionInstanceIdIn;
    private String decisionDefinitionId;
    private String decisionDefinitionIdIn;
    private String decisionDefinitionKey;
    private String decisionDefinitionKeyIn;
    private String decisionDefinitionName;
    private String decisionDefinitionNameLike;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processInstanceId;
    private String caseDefinitionId;
    private String caseDefinitionKey;
    private String caseInstanceId;
    private String activityIdIn;
    private String activityInstanceIdIn;
    private OffsetDateTime evaluatedBefore;
    private OffsetDateTime evaluatedAfter;
    private String userId;
    private String rootDecisionInstanceId;
    private Boolean rootDecisionInstancesOnly;
    private String decisionRequirementsDefinitionId;
    private String decisionRequirementsDefinitionKey;
    private Boolean includeInputs;
    private Boolean includeOutputs;
    private Boolean disableBinaryFetching;
    private Boolean disableCustomObjectDeserialization;

    public HistoricDecisionInstanceQueryImpl(HistoryApiClient historyApiClient) {
        this.historyApiClient = historyApiClient;
    }

    @Override
    public long count() {
        ResponseEntity<CountResultDto> response = historyApiClient.getHistoricDecisionInstancesCount(
                decisionInstanceId, decisionInstanceIdIn, decisionDefinitionId,
                decisionDefinitionIdIn, decisionDefinitionKey, decisionDefinitionKeyIn, decisionDefinitionName,
                decisionDefinitionNameLike, processDefinitionId, processDefinitionKey, processInstanceId,
                caseDefinitionId, caseDefinitionKey, caseInstanceId, activityIdIn, activityInstanceIdIn,
                getTenantIds() != null ? String.join(",", getTenantIds()) : null, getTenantIdsSet(), evaluatedBefore,
                evaluatedAfter, userId, rootDecisionInstanceId,
                rootDecisionInstancesOnly, decisionRequirementsDefinitionId, decisionRequirementsDefinitionKey);
        CountResultDto countResultDto = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && countResultDto != null) {
            return getCountResult(countResultDto);
        }
        log.error("Error on loading decisions count, status code {}", response.getStatusCode());
        return -1;
    }

    @Override
    public List<HistoricDecisionInstance> listPage(int firstResult, int maxResult) {
        QueryOrderingProperty queryOrderingProperty = sortProperty();
        String orderBy = queryOrderingProperty != null ? queryOrderingProperty.getProperty() : null;
        String orderDirection = queryOrderingProperty != null && queryOrderingProperty.getDirection() != null
                ? queryOrderingProperty.getDirection().name().toLowerCase()
                : null;
        ResponseEntity<List<HistoricDecisionInstanceDto>> response = historyApiClient.getHistoricDecisionInstances(
                decisionInstanceId, decisionInstanceIdIn, decisionDefinitionId,
                decisionDefinitionIdIn, decisionDefinitionKey, decisionDefinitionKeyIn, decisionDefinitionName,
                decisionDefinitionNameLike, processDefinitionId, processDefinitionKey, processInstanceId,
                caseDefinitionId, caseDefinitionKey, caseInstanceId, activityIdIn, activityInstanceIdIn,
                getTenantIds() != null ? String.join(",", getTenantIds()) : null, getTenantIdsSet(), evaluatedBefore,
                evaluatedAfter, userId, rootDecisionInstanceId,
                rootDecisionInstancesOnly, decisionRequirementsDefinitionId, decisionRequirementsDefinitionKey,
                includeInputs, includeOutputs, disableBinaryFetching, disableCustomObjectDeserialization,
                orderBy, orderDirection, firstResult, maxResult);
        List<HistoricDecisionInstanceDto> historicDecisionInstanceDtoList = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && historicDecisionInstanceDtoList != null) {
            return historicDecisionInstanceDtoList
                    .stream()
                    .map(e -> (HistoricDecisionInstance) new HistoricDecisionInstanceImpl(
                            e.getId(), e.getDecisionDefinitionId(), e.getDecisionDefinitionKey(),
                            e.getDecisionDefinitionName(), convertOffsetDateTimeToDate(e.getEvaluationTime()),
                            convertOffsetDateTimeToDate(e.getRemovalTime()), e.getProcessDefinitionKey(),
                            e.getProcessDefinitionId(), e.getProcessInstanceId(), e.getCaseDefinitionKey(),
                            e.getCaseDefinitionId(), e.getCaseInstanceId(), e.getActivityId(),
                            e.getActivityInstanceId(), e.getUserId(),
                            getHistoryDecisionInstanceInputs(e.getInputs()),
                            getHistoryDecisionInstanceOutputs(e.getOutputs()),
                            e.getCollectResultValue(), e.getRootDecisionInstanceId(), e.getRootProcessInstanceId(),
                            e.getDecisionRequirementsDefinitionId(), e.getDecisionRequirementsDefinitionKey(),
                            e.getTenantId() ))
                    .toList();
        }
        log.error("Error on loading decisions, status code {}", response.getStatusCode());
        return List.of();
    }

    private Date convertOffsetDateTimeToDate(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            return Date.from(offsetDateTime.toInstant());
        }
        return null;
    }

    private List<HistoricDecisionInputInstance> getHistoryDecisionInstanceInputs(
            List<HistoricDecisionInputInstanceDto> inputs) {
        if (inputs != null) {
           return inputs.stream().map(e -> (HistoricDecisionInputInstance) new HistoricDecisionInputInstanceImpl(
                    e.getId(), e.getDecisionInstanceId(), e.getClauseId(), e.getClauseName(), e.getType(), e.getValue(),
                    null, e.getErrorMessage(), convertOffsetDateTimeToDate(e.getCreateTime()),
                    e.getRootProcessInstanceId(), convertOffsetDateTimeToDate(e.getRemovalTime()))).toList();
        }
        return List.of();
    }

    private List<HistoricDecisionOutputInstance> getHistoryDecisionInstanceOutputs(
            List<HistoricDecisionOutputInstanceDto> outputs) {
        if (outputs != null) {
            return outputs.stream().map(e -> (HistoricDecisionOutputInstance) new HistoricDecisionOutputInstanceImpl(
                    e.getId(), e.getDecisionInstanceId(), e.getClauseId(), e.getClauseName(), e.getRuleId(),
                    e.getRuleOrder(), e.getVariableName(), e.getType(), e.getValue(), null, e.getErrorMessage(),
                    convertOffsetDateTimeToDate(e.getCreateTime()), e.getRootProcessInstanceId(),
                    convertOffsetDateTimeToDate(e.getRemovalTime()))).toList();
        }
        return List.of();
    }

    @Override
    public HistoricDecisionInstanceQuery decisionInstanceId(String decisionInstanceId) {
        this.decisionInstanceId = decisionInstanceId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionInstanceIdIn(String... decisionInstanceIdIn) {
        if (decisionInstanceIdIn != null) {
            this.decisionInstanceIdIn = String.join(",", decisionInstanceIdIn);
        }
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionDefinitionId(String decisionDefinitionId) {
        this.decisionDefinitionId = decisionDefinitionId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionDefinitionIdIn(String... decisionDefinitionIdIn) {
        if (decisionDefinitionIdIn != null) {
            this.decisionDefinitionIdIn = String.join(",", decisionDefinitionIdIn);
        }
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionDefinitionKey(String decisionDefinitionKey) {
        this.decisionDefinitionKey = decisionDefinitionKey;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionDefinitionKeyIn(String... decisionDefinitionKeyIn) {
        if (decisionDefinitionKeyIn != null) {
            this.decisionDefinitionKeyIn = String.join(",", decisionDefinitionKeyIn);
        }
        return null;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionDefinitionName(String decisionDefinitionName) {
        this.decisionDefinitionName = decisionDefinitionName;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionDefinitionNameLike(String decisionDefinitionNameLike) {
        this.decisionDefinitionNameLike = decisionDefinitionNameLike;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery processDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery processDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery processInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery caseDefinitionKey(String caseDefinitionKey) {
        this.caseDefinitionKey = caseDefinitionKey;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery caseDefinitionId(String caseDefinitionId) {
        this.caseDefinitionId = caseDefinitionId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery caseInstanceId(String caseInstanceId) {
        this.caseInstanceId = caseInstanceId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery activityIdIn(String... activityIdIn) {
        if (activityIdIn != null) {
            this.activityIdIn = String.join(",", activityIdIn);
        }
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery activityInstanceIdIn(String... strings) {
        if (strings != null) {
            this.activityInstanceIdIn = String.join(",", strings);
        }
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery evaluatedBefore(Date evaluatedBefore) {
        if (evaluatedBefore != null) {
            this.evaluatedBefore = evaluatedBefore.toInstant().atOffset(ZoneOffset.UTC);
        }
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery evaluatedAfter(Date evaluatedAfter) {
        if (evaluatedAfter != null) {
            this.evaluatedAfter = evaluatedAfter.toInstant().atOffset(ZoneOffset.UTC);
        }
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery userId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery includeInputs() {
        this.includeInputs = true;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery includeOutputs() {
        this.includeOutputs = true;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery disableBinaryFetching() {
        this.disableBinaryFetching = true;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery disableCustomObjectDeserialization() {
        this.disableCustomObjectDeserialization = true;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery rootDecisionInstanceId(String rootDecisionInstanceId) {
        this.rootDecisionInstanceId = rootDecisionInstanceId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery rootDecisionInstancesOnly() {
        this.rootDecisionInstancesOnly = true;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionRequirementsDefinitionId(String decisionRequirementsDefinitionId) {
        this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery decisionRequirementsDefinitionKey(String decisionrequirementsDefinitionKey) {
        this.decisionRequirementsDefinitionKey = decisionrequirementsDefinitionKey;
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery orderByDecisionInstanceId() {
        orderBy("decisionInstanceId");
        return this;
    }

    @Override
    public HistoricDecisionInstanceQuery orderByEvaluationTime() {
        orderBy("evaluationTime");
        return this;
    }

    @Getter
    static class HistoricDecisionInstanceImpl implements HistoricDecisionInstance {

        String id;
        String decisionDefinitionId;
        String decisionDefinitionKey;
        String decisionDefinitionName;
        Date evaluationTime;
        Date removalTime;
        String processDefinitionKey;
        String processDefinitionId;
        String processInstanceId;
        String caseDefinitionKey;
        String caseDefinitionId;
        String caseInstanceId;
        String activityId;
        String activityInstanceId;
        String userId;
        List<HistoricDecisionInputInstance> inputs;
        List<HistoricDecisionOutputInstance> outputs;
        Double collectResultValue;
        String rootDecisionInstanceId;
        String rootProcessInstanceId;
        String decisionRequirementsDefinitionId;
        String decisionRequirementsDefinitionKey;
        String tenantId;

        public HistoricDecisionInstanceImpl(String id, String decisionDefinitionId, String decisionDefinitionKey,
                                            String decisionDefinitionName, Date evaluationTime, Date removalTime,
                                            String processDefinitionKey, String processDefinitionId,
                                            String processInstanceId, String caseDefinitionKey, String caseDefinitionId,
                                            String caseInstanceId, String activityId, String activityInstanceId,
                                            String userId, List<HistoricDecisionInputInstance> inputs,
                                            List<HistoricDecisionOutputInstance> outputs, Double collectResultValue,
                                            String rootDecisionInstanceId, String rootProcessInstanceId,
                                            String decisionRequirementsDefinitionId,
                                            String decisionRequirementsDefinitionKey, String tenantId) {
            this.id = id;
            this.decisionDefinitionId = decisionDefinitionId;
            this.decisionDefinitionKey = decisionDefinitionKey;
            this.decisionDefinitionName = decisionDefinitionName;
            this.evaluationTime = evaluationTime;
            this.removalTime = removalTime;
            this.processDefinitionKey = processDefinitionKey;
            this.processDefinitionId = processDefinitionId;
            this.processInstanceId = processInstanceId;
            this.caseDefinitionKey = caseDefinitionKey;
            this.caseDefinitionId = caseDefinitionId;
            this.caseInstanceId = caseInstanceId;
            this.activityId = activityId;
            this.activityInstanceId = activityInstanceId;
            this.userId = userId;
            this.inputs = inputs;
            this.outputs = outputs;
            this.collectResultValue = collectResultValue;
            this.rootDecisionInstanceId = rootDecisionInstanceId;
            this.rootProcessInstanceId = rootProcessInstanceId;
            this.decisionRequirementsDefinitionId = decisionRequirementsDefinitionId;
            this.decisionRequirementsDefinitionKey = decisionRequirementsDefinitionKey;
            this.tenantId = tenantId;
        }
    }

    @Getter
    static class HistoricDecisionInputInstanceImpl implements HistoricDecisionInputInstance {
        String id;
        String decisionInstanceId;
        String clauseId;
        String clauseName;
        String typeName;
        Object value;
        TypedValue typedValue;
        String errorMessage;
        Date createTime;
        String rootProcessInstanceId;
        Date removalTime;

        public HistoricDecisionInputInstanceImpl(String id, String decisionInstanceId, String clauseId,
                                                 String clauseName, String typeName, Object value,
                                                 TypedValue typedValue, String errorMessage,
                                                 Date createTime, String rootProcessInstanceId, Date removalTime) {
            this.id = id;
            this.decisionInstanceId = decisionInstanceId;
            this.clauseId = clauseId;
            this.clauseName = clauseName;
            this.typeName = typeName;
            this.value = value;
            this.typedValue = typedValue;
            this.errorMessage = errorMessage;
            this.createTime = createTime;
            this.rootProcessInstanceId = rootProcessInstanceId;
            this.removalTime = removalTime;
        }
    }

    @Getter
    static class HistoricDecisionOutputInstanceImpl implements HistoricDecisionOutputInstance {
        String id;
        String decisionInstanceId;
        String clauseId;
        String clauseName;
        String ruleId;
        Integer ruleOrder;
        String variableName;
        String typeName;
        Object value;
        TypedValue typedValue;
        String errorMessage;
        Date createTime;
        String rootProcessInstanceId;
        Date removalTime;

        public HistoricDecisionOutputInstanceImpl(String id, String decisionInstanceId, String clauseId,
                                                  String clauseName, String ruleId, Integer ruleOrder,
                                                  String variableName, String typeName, Object value,
                                                  TypedValue typedValue, String errorMessage, Date createTime,
                                                  String rootProcessInstanceId, Date removalTime) {
            this.id = id;
            this.decisionInstanceId = decisionInstanceId;
            this.clauseId = clauseId;
            this.clauseName = clauseName;
            this.ruleId = ruleId;
            this.ruleOrder = ruleOrder;
            this.variableName = variableName;
            this.typeName = typeName;
            this.value = value;
            this.typedValue = typedValue;
            this.errorMessage = errorMessage;
            this.createTime = createTime;
            this.rootProcessInstanceId = rootProcessInstanceId;
            this.removalTime = removalTime;
        }
    }
}
