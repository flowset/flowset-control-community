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

    /**
     * Calculates the progress of the batch execution in percentage.
     *
     * @return progress of batch execution in percents
     */
    public int getProgressPercent() {
        int totalJobs = this.totalJobs != null && this.totalJobs > 0 ? this.totalJobs : 0;
        int completedJobs = this.completedJobs != null ? this.completedJobs : 0;

        int percent;
        if (totalJobs == 0) {
            percent = 0;
        } else {
            double ratio = completedJobs / (double) totalJobs;
            percent = (int) Math.round(ratio * 100);
        }
        return percent;
    }
}
