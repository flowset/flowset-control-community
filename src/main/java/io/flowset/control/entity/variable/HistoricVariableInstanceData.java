/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.variable;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.PropertyDatatype;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.OffsetDateTime;

@JmixEntity(annotatedPropertiesOnly = true)
public class HistoricVariableInstanceData {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    @JmixProperty
    protected String historicVariableInstanceId;

    @JmixProperty
    protected String processInstanceId;

    @JmixProperty
    protected String taskId;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    @JmixProperty
    protected OffsetDateTime createTime;

    @JmixProperty
    protected VariableValueInfo valueInfo;

    @JmixProperty
    protected String type;

    @JmixProperty
    protected String name;

    @JmixProperty
    protected String processDefinitionKey;

    @JmixProperty
    protected String processDefinitionId;

    @JmixProperty
    protected String executionId;

    @JmixProperty
    protected String activityInstanceId;

    @JmixProperty
    protected String caseDefinitionKey;

    @JmixProperty
    protected String caseDefinitionId;

    @JmixProperty
    protected String caseInstanceId;

    @JmixProperty
    protected String caseExecutionId;

    @JmixProperty
    protected String tenantId;

    @JmixProperty
    protected String errorMessage;

    @JmixProperty
    protected String state;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @JmixProperty
    protected OffsetDateTime removalTime;

    @JmixProperty
    protected String rootProcessInstanceId;

    protected Object value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHistoricVariableInstanceId() {
        return historicVariableInstanceId;
    }

    public void setHistoricVariableInstanceId(String historicVariableInstanceId) {
        this.historicVariableInstanceId = historicVariableInstanceId;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }

    public VariableValueInfo getValueInfo() {
        return valueInfo;
    }

    public void setValueInfo(VariableValueInfo valueInfo) {
        this.valueInfo = valueInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    public String getCaseDefinitionKey() {
        return caseDefinitionKey;
    }

    public void setCaseDefinitionKey(String caseDefinitionKey) {
        this.caseDefinitionKey = caseDefinitionKey;
    }

    public String getCaseDefinitionId() {
        return caseDefinitionId;
    }

    public void setCaseDefinitionId(String caseDefinitionId) {
        this.caseDefinitionId = caseDefinitionId;
    }

    public String getCaseInstanceId() {
        return caseInstanceId;
    }

    public void setCaseInstanceId(String caseInstanceId) {
        this.caseInstanceId = caseInstanceId;
    }

    public String getCaseExecutionId() {
        return caseExecutionId;
    }

    public void setCaseExecutionId(String caseExecutionId) {
        this.caseExecutionId = caseExecutionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public OffsetDateTime getRemovalTime() {
        return removalTime;
    }

    public void setRemovalTime(OffsetDateTime removalTime) {
        this.removalTime = removalTime;
    }

    public String getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(String rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }
}
