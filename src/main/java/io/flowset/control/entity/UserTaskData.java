/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.*;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.OffsetDateTime;

@JmixEntity(name = "bpm_UserTaskData")
public class UserTaskData {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    protected String taskId;

    protected String taskDefinitionKey;

    protected String formKey;

    protected String caseDefinitionId;

    protected String caseExecutionId;

    protected String processDefinitionId;

    protected String processDefinitionKey;

    protected String processInstanceId;

    protected String processExecutionId;

    protected String name;

    protected String description;

    protected Integer priority;

    protected String assignee;

    protected String owner;

    protected String parentTaskId;

    protected String caseInstanceId;

    protected Boolean suspended;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    protected OffsetDateTime startTime;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    protected OffsetDateTime createTime;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    protected OffsetDateTime dueDate;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    protected OffsetDateTime followUpDate;

    protected String tenantId;

    protected String delegationState;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    protected OffsetDateTime lastUpdateDate;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime endTime;

    protected String executionId;

    protected String caseDefinitionKey;

    protected String activityInstanceId;

    protected String deleteReason;

    protected Long duration;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime removalTime;

    protected String rootProcessInstanceId;

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public OffsetDateTime getFollowUpDate() {
        return followUpDate;
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public String getCaseDefinitionId() {
        return caseDefinitionId;
    }

    public void setCaseDefinitionId(String caseDefinitionId) {
        this.caseDefinitionId = caseDefinitionId;
    }

    public String getCaseExecutionId() {
        return caseExecutionId;
    }

    public void setCaseExecutionId(String caseExecutionId) {
        this.caseExecutionId = caseExecutionId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessExecutionId() {
        return processExecutionId;
    }

    public void setProcessExecutionId(String processExecutionId) {
        this.processExecutionId = processExecutionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getCaseInstanceId() {
        return caseInstanceId;
    }

    public void setCaseInstanceId(String caseInstanceId) {
        this.caseInstanceId = caseInstanceId;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public void setFollowUpDate(OffsetDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }

    public void setLastUpdateDate(OffsetDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDelegationState() {
        return delegationState;
    }

    public void setDelegationState(String delegationState) {
        this.delegationState = delegationState;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getCaseDefinitionKey() {
        return caseDefinitionKey;
    }

    public void setCaseDefinitionKey(String caseDefinitionKey) {
        this.caseDefinitionKey = caseDefinitionKey;
    }

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
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

    @InstanceName
    @DependsOnProperties({"name", "description"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(name),
                metadataTools.format(description));
    }
}
