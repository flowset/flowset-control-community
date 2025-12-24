/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.job;

import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobDefinitionData;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Provides methods to get and update jobs data in the BPM engine.
 */
public interface JobService {

    /**
     * Loads running jobs from the engine using the specified context.
     *
     * @param loadContext a context to load running jobs
     * @return a list of running jobs
     */
    List<JobData> findAll(JobLoadContext loadContext);

    /**
     * Loads from engine the total count of running jobs that match the specified filter.
     *
     * @param filter a job filter instance
     * @return count of running incidents
     */
    long getCount(@Nullable JobFilter filter);

    /**
     * Retrieves a job by its identifier from the engine.
     *
     * @param jobId the unique identifier of the job
     * @return the {@code JobData} corresponding to the specified job identifier,
     *         or {@code null} if no such job exists
     */
    JobData findById(String jobId);

    /**
     * Loads a job definition with the specified identifier.
     *
     * @param jobDefinitionId a job definition identifier
     * @return found job definition or null if not found
     */
    @Nullable
    JobDefinitionData findJobDefinition(String jobDefinitionId);

    /**
     * Updates the retry count to the specified value for the job with the specified identifier.
     *
     * @param jobId   a job identifier
     * @param retries a new value of retries
     */
    void setJobRetries(String jobId, int retries);

    /**
     * Asynchronously updates the retry count to the specified value for the jobs with the specified identifiers.
     *
     * @param jobIds  a list of job identifiers
     * @param retries a new value of retries
     */
    void setJobRetriesAsync(List<String> jobIds, int retries);

    /**
     * Loads error details of the running job with the specified identifier.
     *
     * @param jobId an identifier of running job
     * @return error details
     */
    String getErrorDetails(String jobId);

    /**
     * Loads from the engine history the error details of the job with the specified identifier.
     *
     * @param jobId a job identifier
     * @return error details
     */
    String getHistoryErrorDetails(String jobId);


    /**
     * Loads from the engine history the job with the specified identifier.
     *
     * @param jobId a job identifier
     * @return boolean
     */
    boolean isHistoryJobLogPresent(String jobId);

    /**
     * Activates a job with the specified identifier.
     *
     * @param jobId the identifier of the job to be activated
     */
    void activateJob(String jobId);

    /**
     * Suspends a job with the specified identifier.
     *
     * @param jobId the identifier of the job to be suspended
     */
    void suspendJob(String jobId);
}
