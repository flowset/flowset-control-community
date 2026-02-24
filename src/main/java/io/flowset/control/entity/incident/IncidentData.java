/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.incident;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.apache.commons.lang3.Strings;
import org.camunda.bpm.engine.runtime.Incident;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
public class IncidentData {
    @JmixGeneratedValue
    @JmixId
    protected UUID id;

    protected String incidentId;

    protected OffsetDateTime timestamp;

    protected String activityId;

    protected String type;

    protected String message;

    protected String executionId;

    protected String failedActivityId;

    protected String processInstanceId;

    protected String processDefinitionId;

    protected String causeIncidentId;

    protected String rootCauseIncidentId;

    protected String jobDefinitionId;

    protected String configuration;

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getJobDefinitionId() {
        return jobDefinitionId;
    }

    public void setJobDefinitionId(String jobDefinitionId) {
        this.jobDefinitionId = jobDefinitionId;
    }

    public String getRootCauseIncidentId() {
        return rootCauseIncidentId;
    }

    public void setRootCauseIncidentId(String rootCauseIncidentId) {
        this.rootCauseIncidentId = rootCauseIncidentId;
    }

    public String getCauseIncidentId() {
        return causeIncidentId;
    }

    public void setCauseIncidentId(String causeIncidentId) {
        this.causeIncidentId = causeIncidentId;
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

    public String getFailedActivityId() {
        return failedActivityId;
    }

    public void setFailedActivityId(String failedActivityId) {
        this.failedActivityId = failedActivityId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isJobFailed() {
        return Strings.CS.equals(type, Incident.FAILED_JOB_HANDLER_TYPE);
    }

    public boolean isExternalTaskFailed() {
        return Strings.CS.equals(type, Incident.EXTERNAL_TASK_HANDLER_TYPE);
    }
}