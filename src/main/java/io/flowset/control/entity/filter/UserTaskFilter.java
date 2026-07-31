/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.filter;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.PropertyDatatype;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
public class UserTaskFilter {
    @JmixGeneratedValue
    @JmixId
    protected UUID id;

    protected String processInstanceId;

    protected String activityInstanceId;

    protected String processDefinitionKey;

    protected String taskKeyLike;

    protected String taskNameLike;

    protected String assigneeLike;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime createdAfter;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime createdBefore;

    protected Boolean assigned;

    protected Boolean unassigned;

    protected Boolean active;

    protected Boolean suspended;

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getAssigneeLike() {
        return assigneeLike;
    }


    public Boolean getUnassigned() {
        return unassigned;
    }

    public void setUnassigned(Boolean unassigned) {
        this.unassigned = unassigned;
    }

    public Boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }


    public OffsetDateTime getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(OffsetDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }

    public OffsetDateTime getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(OffsetDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }

    public void setAssigneeLike(String assigneeLike) {
        this.assigneeLike = assigneeLike;
    }

    public String getTaskNameLike() {
        return taskNameLike;
    }

    public void setTaskNameLike(String taskNameLike) {
        this.taskNameLike = taskNameLike;
    }

    public String getTaskKeyLike() {
        return taskKeyLike;
    }

    public void setTaskKeyLike(String taskKeyLike) {
        this.taskKeyLike = taskKeyLike;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}