/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.batch;

import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

@JmixEntity
@Getter
@Setter
public class BatchStatisticsData extends RuntimeBatchData {
    protected Integer remainingJobs;
    protected Integer completedJobs;
    protected Integer failedJobs;

    public boolean isFailed() {
        return failedJobs != null && failedJobs > 0;
    }
}
