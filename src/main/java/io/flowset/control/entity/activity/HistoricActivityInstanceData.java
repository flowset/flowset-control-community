/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.activity;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.PropertyDatatype;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.OffsetDateTime;

@JmixEntity
public class HistoricActivityInstanceData {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    private String activityInstanceId;

    protected String activityId;

    @InstanceName
    protected String activityName;

    protected String activityType;

    protected String processDefinitionId;

    protected String processInstanceId;

    protected String executionId;

    protected String taskId;

    protected String calledProcessInstanceId;

    protected String assignee;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    protected OffsetDateTime startTime;

    @PropertyDatatype(value = "engineOffsetDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    protected OffsetDateTime endTime;

    protected Long durationInMillis;

    private Boolean cancelled;

    private Boolean completeScope;

    public Boolean getCompleteScope() {
        return completeScope;
    }

    public void setCompleteScope(Boolean completeScope) {
        this.completeScope = completeScope;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
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

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getCalledProcessInstanceId() {
        return calledProcessInstanceId;
    }

    public void setCalledProcessInstanceId(String calledProcessInstanceId) {
        this.calledProcessInstanceId = calledProcessInstanceId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationInMillis() {
        return durationInMillis;
    }

    public void setDurationInMillis(Long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }

}
