/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.activity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@JmixEntity
public class ActivityInstanceTreeItem {
    @JmixGeneratedValue
    @JmixId
    protected UUID id;

    protected ActivityInstanceTreeItem parentActivityInstance;

    protected String activityInstanceId;

    protected String activityId;

    protected String activityName;

    protected String activityType;

    protected String processInstanceId;

    protected String processDefinitionId;

    protected List<String> executionIds;

    protected Boolean transition;

    public Boolean getTransition() {
        return transition;
    }

    public ActivityInstanceTreeItem getParentActivityInstance() {
        return parentActivityInstance;
    }

    public void setParentActivityInstance(ActivityInstanceTreeItem parentActivityInstance) {
        this.parentActivityInstance = parentActivityInstance;
    }

    public void setTransition(Boolean transition) {
        this.transition = transition;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public List<String> getExecutionIds() {
        return executionIds;
    }

    public void setExecutionIds(List<String> executionIds) {
        this.executionIds = executionIds;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JmixProperty
    @Nullable
    public String getScopeExecutionId() {
        return CollectionUtils.isNotEmpty(executionIds) ? executionIds.getFirst() : null;
    }
}