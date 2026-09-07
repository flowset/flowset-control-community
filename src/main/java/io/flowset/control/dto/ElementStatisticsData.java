/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.dto;

import io.flowset.uikit.component.bpmnviewer.model.ActivityInstanceStatisticsData;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains statistics for one process instance element.
 */
@Getter
@Setter
public class ElementStatisticsData implements ActivityInstanceStatisticsData {
    protected String elementId;
    protected Integer activeCount;
    protected Integer completedCount;
    protected Integer incidentCount;

    public ElementStatisticsData(String elementId) {
        this.elementId = elementId;
    }

    /**
     * Increment the count of finished activity instances.
     *
     * @return current instance
     */
    public ElementStatisticsData incrementFinishedCount() {
        if (this.completedCount == null) {
            this.completedCount = 0;
        }
        this.completedCount++;
        return this;
    }

    /**
     * Increment the count of active activity instances.
     *
     * @return current instance
     */
    public ElementStatisticsData incrementActiveCount() {
        if (this.activeCount == null) {
            this.activeCount = 0;
        }
        this.activeCount++;
        return this;
    }
}
