/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.job;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.UUID;

@JmixEntity
public class JobDefinitionData {

    @JmixId
    protected String id;

    protected String jobDefinitionId;

    protected String jobType;

    protected String activityId;

    public String getJobDefinitionId() {
        return jobDefinitionId;
    }

    public void setJobDefinitionId(String jobDefinitionId) {
        this.jobDefinitionId = jobDefinitionId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}