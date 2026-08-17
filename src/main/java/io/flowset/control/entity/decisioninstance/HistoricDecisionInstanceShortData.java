package io.flowset.control.entity.decisioninstance;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.PropertyDatatype;

import java.time.OffsetDateTime;
import java.util.List;

@JmixEntity
public class HistoricDecisionInstanceShortData {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    protected String decisionInstanceId;

    protected String decisionDefinitionId;

    protected String decisionDefinitionKey;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime evaluationTime;

    protected String processDefinitionKey;

    protected String processDefinitionId;

    protected String processInstanceId;

    protected String activityId;

    protected String activityInstanceId;

    protected List<HistoricDecisionInputInstanceShortData> inputs;

    protected List<HistoricDecisionOutputInstanceShortData> outputs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDecisionInstanceId() {
        return decisionInstanceId;
    }

    public void setDecisionInstanceId(String decisionInstanceId) {
        this.decisionInstanceId = decisionInstanceId;
    }

    public String getDecisionDefinitionId() {
        return decisionDefinitionId;
    }

    public void setDecisionDefinitionId(String decisionDefinitionId) {
        this.decisionDefinitionId = decisionDefinitionId;
    }

    public String getDecisionDefinitionKey() {
        return decisionDefinitionKey;
    }

    public void setDecisionDefinitionKey(String decisionDefinitionKey) {
        this.decisionDefinitionKey = decisionDefinitionKey;
    }

    public OffsetDateTime getEvaluationTime() {
        return evaluationTime;
    }

    public void setEvaluationTime(OffsetDateTime evaluationTime) {
        this.evaluationTime = evaluationTime;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    public List<HistoricDecisionInputInstanceShortData> getInputs() {
        return inputs;
    }

    public void setInputs(List<HistoricDecisionInputInstanceShortData> inputs) {
        this.inputs = inputs;
    }

    public List<HistoricDecisionOutputInstanceShortData> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<HistoricDecisionOutputInstanceShortData> outputs) {
        this.outputs = outputs;
    }
}
