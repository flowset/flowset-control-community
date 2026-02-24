/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.externaltask;

import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.entity.filter.ExternalTaskFilter;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Provides methods for getting and updating data about external task instances in the BPM engine.
 */
public interface ExternalTaskService {
    /**
     * Loads running external task instances from the engine using the specified context.
     *
     * @param loadContext a context to load external task instances
     * @return found running external tasks
     */
    List<ExternalTaskData> findRunningTasks(ExternalTaskLoadContext loadContext);

    /**
     * Loads an external task instance with the specified identifier from the engine.
     *
     * @param externalTaskId external task identifier
     * @return external task instance or null
     */
    @Nullable
    ExternalTaskData findById(String externalTaskId);

    /**
     * Loads from engine the total count of external task instances that match the specified filter.
     *
     * @param filter an external task filter instance
     * @return count of external task instances
     */
    long getRunningTasksCount(@Nullable ExternalTaskFilter filter);

    /**
     * Updates the retry count to the specified value for the external task instance with the specified identifier.
     *
     * @param externalTaskId an external task instance identifier
     * @param retries        a new value of retries
     */
    void setRetries(String externalTaskId, int retries);

    /**
     * Asynchronously updates the retry count to the specified value for external task instances with the specified identifiers.
     *
     * @param externalTaskIds a list of external task instance identifiers
     * @param retries         a new value of retries
     */
    void setRetriesAsync(List<String> externalTaskIds, int retries);

    /**
     * Loads error details for the running external task instance with the specified identifier.
     *
     * @param externalTaskId an identifier of running external task instance
     * @return error details
     */
    String getErrorDetails(String externalTaskId);
}