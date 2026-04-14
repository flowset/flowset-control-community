/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.batch;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;

import java.time.OffsetDateTime;

@JmixEntity
@Getter
@Setter
public class RuntimeBatchData {
    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    protected String type;

    protected Integer totalJobs;

    protected Integer jobsCreated;

    protected Integer batchJobsPerSeed;

    protected Integer invocationsPerBatchJob;

    protected String seedJobDefinitionId;

    protected String monitorJobDefinitionId;

    protected String batchJobDefinitionId;

    protected Boolean suspended;

    protected String tenantId;

    protected String createUserId;

    protected OffsetDateTime startTime;

    protected OffsetDateTime executionStartTime;

    @JmixProperty
    public BatchState getState() {
        return BooleanUtils.isTrue(suspended) ? BatchState.SUSPENDED : BatchState.ACTIVE;
    }
}
