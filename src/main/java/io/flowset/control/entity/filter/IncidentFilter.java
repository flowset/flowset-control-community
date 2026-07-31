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
public class IncidentFilter {
    @JmixGeneratedValue
    @JmixId
    protected UUID id;

    protected String activityId;

    protected String incidentId;

    protected String incidentType;

    protected String processInstanceId;

    protected String processDefinitionId;

    protected String processDefinitionKey;

    protected String incidentMessageLike;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime incidentTimestampAfter;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime incidentTimestampBefore;

    public void setIncidentTimestampBefore(OffsetDateTime incidentTimestampBefore) {
        this.incidentTimestampBefore = incidentTimestampBefore;
    }

    public OffsetDateTime getIncidentTimestampBefore() {
        return incidentTimestampBefore;
    }

    public OffsetDateTime getIncidentTimestampAfter() {
        return incidentTimestampAfter;
    }

    public void setIncidentTimestampAfter(OffsetDateTime incidentTimestampAfter) {
        this.incidentTimestampAfter = incidentTimestampAfter;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getIncidentMessageLike() {
        return incidentMessageLike;
    }

    public void setIncidentMessageLike(String incidentMessageLike) {
        this.incidentMessageLike = incidentMessageLike;
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

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
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