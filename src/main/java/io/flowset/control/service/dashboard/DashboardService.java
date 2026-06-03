/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.dashboard;

import io.flowset.control.entity.ProcessExecutionGraphEntry;
import io.flowset.control.entity.UserTaskData;
import io.flowset.control.entity.dashboard.ProcessDefinitionStatistics;
import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.property.UiProperties;
import io.flowset.control.security.SecuredEntityLoad;

import java.util.List;

/**
 * Provides methods to load a data from BPM engine required to show it on dashboard.
 */
public interface DashboardService {

    /**
     * Loads statistics for the period that contain the number of running and completed process instances by day.
     * Period end date - end of previous day, period start date - previous day minus number of days set in {@link UiProperties#getRecentActivityDays()}.
     *
     * @param bpmEngine a BPM engine from which data needs to be loaded
     * @return a list of statistics items, each containing the number of processes started and completed per day
     * @see UiProperties#getRecentActivityDays()
     * @see UiProperties#getRecentActivityMaxResults()
     */
    @SecuredEntityLoad(entityClass = ProcessInstanceData.class)
    List<ProcessExecutionGraphEntry> getRecentActivityStatistics(BpmEngine bpmEngine);

    /**
     * Loads a count of active user tasks on the specified engine.
     *
     * @param bpmEngine a BPM engine from which data needs to be loaded
     * @return count of active user tasks
     */
    @SecuredEntityLoad(entityClass = UserTaskData.class)
    long getUserTasksCount(BpmEngine bpmEngine);

    /**
     * Loads a count of processes deployed on the specified BPM engine. Only the latest versions of processes are taken into account.
     *
     * @param bpmEngine a BPM engine from which data needs to be loaded
     * @return count of deployed processes
     */
    @SecuredEntityLoad(entityClass = ProcessDefinitionData.class)
    long getDeployedProcessesCount(BpmEngine bpmEngine);

    /**
     * Loads a count of process instances running on the specified BPM engine.
     *
     * @param bpmEngine a BPM engine from which data needs to be loaded
     * @return count of running process instances
     */
    @SecuredEntityLoad(entityClass = ProcessInstanceData.class)
    long getRunningProcessCount(BpmEngine bpmEngine);

    /**
     * Loads a count of process instances suspended on the specified BPM engine.
     *
     * @param bpmEngine a BPM engine from which data needs to be loaded
     * @return count of suspended process instances
     */
    @SecuredEntityLoad(entityClass = ProcessInstanceData.class)
    long getSuspendedProcessCount(BpmEngine bpmEngine);

    /**
     * Loads from the specified BPM engine the statistics about processes including information about open incidents and failed jobs.
     *
     * @param bpmEngine a BPM engine from which data needs to be loaded
     * @return a list of item each of the includes statistics by the one process definition
     */
    @SecuredEntityLoad(entityClass = ProcessDefinitionStatistics.class)
    List<ProcessDefinitionStatistics> getProcessDefinitionStatistics(BpmEngine bpmEngine);
}
