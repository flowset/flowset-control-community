/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.property;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "flowset.control.ui")
@ConfigurationPropertiesBinding
public class UiProperties {

    /**
     * A timeout (in seconds) for asynchronously loading data from the BPM engine for the dashboard.
     */
    private final int dashboardLoadTimeoutSec;

    /**
     * A timeout (in seconds) for asynchronously loading engine time to show in screens if engine is not available.
     */
    private final int engineTimeLoadTimeoutSec;

    /**
     * A maximum number of records loaded for the Recent activity chart. The value is applied for started and completed process instances.
     */
    @Positive
    private final int recentActivityMaxResults;

    /**
     * A period for which data should be shown in the Recent activity chart. The current day is not included.
     * <b>Note: do not use large values due to possible performance issues</b>
     */
    @PositiveOrZero
    private final int recentActivityDays;

    /**
     * A maximum number of files that can be uploaded to a single deployment in the Upload deployment view.
     */
    @Positive
    private final int deploymentUploadMaxFiles;

    /**
     * A maximum size (in bytes) of a single file uploaded in the Upload deployment view.
     */
    @Positive
    private final long deploymentUploadMaxFileSizeBytes;

    public UiProperties(@DefaultValue("300") int dashboardLoadTimeoutSec,
                        @DefaultValue("500") int recentActivityMaxResults,
                        @DefaultValue("7") int recentActivityDays,
                        @DefaultValue("300") int engineTimeLoadTimeoutSec,
                        @DefaultValue("50") int deploymentUploadMaxFiles,
                        @DefaultValue("10485760") long deploymentUploadMaxFileSizeBytes) {
        this.dashboardLoadTimeoutSec = dashboardLoadTimeoutSec;
        this.recentActivityMaxResults = recentActivityMaxResults;
        this.recentActivityDays = recentActivityDays;
        this.engineTimeLoadTimeoutSec = engineTimeLoadTimeoutSec;
        this.deploymentUploadMaxFiles = deploymentUploadMaxFiles;
        this.deploymentUploadMaxFileSizeBytes = deploymentUploadMaxFileSizeBytes;
    }

    /**
     * @return a timeout for asynchronously loading data for the dashboard
     */
    public int getDashboardLoadTimeoutSec() {
        return dashboardLoadTimeoutSec;
    }

    /**
     * @return a maximum number of records loaded for the Recent activity chart
     */
    public int getRecentActivityMaxResults() {
        return recentActivityMaxResults;
    }

    /**
     * @return a period for which for the Recent activity chart should be shown
     */
    public int getRecentActivityDays() {
        return recentActivityDays;
    }

    /**
     * @return a timeout for asynchronously loading engine time for screens
     */
    public int getEngineTimeLoadTimeoutSec() {
        return engineTimeLoadTimeoutSec;
    }

    /**
     * @return a maximum number of files that can be uploaded to a single deployment
     */
    public int getDeploymentUploadMaxFiles() {
        return deploymentUploadMaxFiles;
    }

    /**
     * @return a maximum size (in bytes) of a single uploaded deployment file
     */
    public long getDeploymentUploadMaxFileSizeBytes() {
        return deploymentUploadMaxFileSizeBytes;
    }
}