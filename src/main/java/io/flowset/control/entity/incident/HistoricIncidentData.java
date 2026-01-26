/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.incident;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.Incident;

import java.time.OffsetDateTime;

@JmixEntity
public class HistoricIncidentData {

    @JmixId
    protected String id;

    protected String incidentId;

    protected String type;

    protected String processDefinitionKey;

    protected String processDefinitionId;

    protected String processInstanceId;

    protected OffsetDateTime createTime;

    protected OffsetDateTime endTime;

    protected Boolean open;

    protected Boolean resolved;

    protected Boolean deleted;

    protected String jobDefinitionId;

    protected String tenantId;

    protected String activityId;

    protected String causeIncidentId;

    protected String rootCauseIncidentId;

    protected String configuration;

    protected String message;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getJobDefinitionId() {
        return jobDefinitionId;
    }

    public void setJobDefinitionId(String jobDefinitionId) {
        this.jobDefinitionId = jobDefinitionId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
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

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isJobFailed() {
        return StringUtils.equals(type, Incident.FAILED_JOB_HANDLER_TYPE);
    }

    public boolean isExternalTaskFailed() {
        return StringUtils.equals(type, Incident.EXTERNAL_TASK_HANDLER_TYPE);
    }
}