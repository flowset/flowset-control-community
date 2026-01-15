/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.job.impl;

import com.google.common.base.Strings;
import feign.FeignException;
import feign.utils.ExceptionUtils;
import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobDefinitionData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.mapper.JobMapper;
import io.flowset.control.service.client.EngineRestClient;
import io.flowset.control.service.engine.EngineTenantProvider;
import io.flowset.control.service.job.JobLoadContext;
import io.flowset.control.service.job.JobService;
import io.jmix.core.Sort;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.community.rest.client.api.HistoryApiClient;
import org.camunda.community.rest.client.api.JobApiClient;
import org.camunda.community.rest.client.api.JobDefinitionApiClient;
import org.camunda.community.rest.client.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.flowset.control.util.EngineRestUtils.getCountResult;
import static io.flowset.control.util.ExceptionUtils.isConnectionError;

@Service("control_JobService")
@Slf4j
public class JobServiceImpl implements JobService {
    protected final JobMapper jobMapper;
    protected final JobApiClient jobApiClient;
    protected final JobDefinitionApiClient jobDefinitionApiClient;
    protected final HistoryApiClient historyApiClient;
    protected final EngineRestClient engineRestClient;
    protected final EngineTenantProvider engineTenantProvider;

    public JobServiceImpl(JobMapper jobMapper,
                          JobApiClient jobApiClient,
                          JobDefinitionApiClient jobDefinitionApiClient,
                          HistoryApiClient historyApiClient,
                          EngineRestClient engineRestClient, EngineTenantProvider engineTenantProvider) {
        this.jobMapper = jobMapper;
        this.jobApiClient = jobApiClient;
        this.jobDefinitionApiClient = jobDefinitionApiClient;
        this.historyApiClient = historyApiClient;
        this.engineRestClient = engineRestClient;
        this.engineTenantProvider = engineTenantProvider;
    }

    @Override
    public List<JobData> findAll(JobLoadContext loadContext) {
        JobQueryDto jobQueryDto = createJobQueryDto(loadContext.getFilter());
        jobQueryDto.setSorting(createSortOptions(loadContext.getSort()));

        try {
            ResponseEntity<List<JobDto>> jobsResponse = jobApiClient.queryJobs(loadContext.getFirstResult(), loadContext.getMaxResults(),
                    jobQueryDto);
            if (jobsResponse.getStatusCode().is2xxSuccessful()) {
                List<JobDto> jobDtoList = jobsResponse.getBody();
                return CollectionUtils.emptyIfNull(jobDtoList)
                        .stream()
                        .map(jobMapper::fromJobDto)
                        .toList();
            }
            log.error("Error on loading runtime jobs: query {}, status code {}", jobQueryDto, jobsResponse.getStatusCode());
            return List.of();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load runtime jobs because BPM engine not selected");
                return List.of();
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load runtime jobs because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public long getCount(@Nullable JobFilter jobFilter) {
        try {
            JobQueryDto jobQueryDto = createJobQueryDto(jobFilter);

            ResponseEntity<CountResultDto> jobsResponse = jobApiClient.queryJobsCount(jobQueryDto);
            if (jobsResponse.getStatusCode().is2xxSuccessful()) {
                return getCountResult(jobsResponse.getBody());
            }
            log.error("Error on loading runtime jobs count: query {}, status code {}", jobQueryDto, jobsResponse.getStatusCode());
            return 0;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable get job count because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }

            throw e;
        }
    }

    @Override
    public JobData findById(String jobId) {
        try {
            ResponseEntity<JobDto> job = jobApiClient.getJob(jobId);
            JobDto body = job.getBody();
            if (body != null) {
                return jobMapper.fromJobDto(body);
            }
            return null;
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof EngineNotSelectedException) {
                log.warn("Unable to load runtime job by id {} because BPM engine not selected", jobId);
                return null;
            }
            if (isConnectionError(rootCause)) {
                log.error("Unable to load runtime job by id {} because of connection error: ", jobId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    @Nullable
    public JobDefinitionData findJobDefinition(String jobDefinitionId) {
        ResponseEntity<JobDefinitionDto> response = jobDefinitionApiClient.getJobDefinition(jobDefinitionId);
        if (response.getStatusCode().is2xxSuccessful()) {
            JobDefinitionDto jobDefinitionDto = response.getBody();
            return jobDefinitionDto != null ? jobMapper.fromJobDefinitionDto(jobDefinitionDto) : null;
        }
        log.error("Error on loading stacktrace for job definition id {}, status code {}", jobDefinitionId, response.getStatusCode());
        return null;
    }

    @Override
    public void setJobRetries(String jobId, int retries) {
        try {
            ResponseEntity<Void> response = jobApiClient.setJobRetries(jobId, new JobRetriesDto().retries(retries));
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error on loading update retries to {} for job with id {}, status code {}", retries,
                        jobId, response.getStatusCode());
            } else {
                log.debug("Update retries count for job {}. New value: {}", jobId, retries);
            }
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable update retries for job '{}' because of connection error: ",  jobId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void setJobRetriesAsync(List<String> jobIds, int retries) {
        try {
            ResponseEntity<BatchDto> response = jobApiClient.setJobRetriesAsyncOperation(new SetJobRetriesDto()
                    .jobIds(jobIds)
                    .retries(retries));

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error on loading update retries to {} for job with ids {}, status code {}", retries,
                        jobIds, response.getStatusCode());
            } else {
                log.debug("Async update retries count for jobs {}. New value: {}", jobIds, retries);
            }
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable update job retries because of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public String getErrorDetails(String jobId) {
        try {
            ResponseEntity<String> response = engineRestClient.getStacktrace(jobId);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Strings.nullToEmpty(response.getBody());
            }
            return "";
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable update get job retries details of connection error: ", e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public String getHistoryErrorDetails(String jobId) {
        ResponseEntity<String> response = engineRestClient.getStacktraceHistoricJobLog(jobId);
        if (response.getStatusCode().is2xxSuccessful()) {
            return Strings.nullToEmpty(response.getBody());
        }
        return "";
    }

    @Override
    public boolean isHistoryJobLogPresent(String jobId) {
        try {
            ResponseEntity<HistoricJobLogDto> jobLogResponse = historyApiClient.getHistoricJobLog(jobId);
            return jobLogResponse.getStatusCode().is2xxSuccessful() && jobLogResponse.getBody() != null;
        } catch (FeignException e) {
            log.error("Error checking job log presence for jobId: {}, error: ", jobId, e);
            return false;
        }
    }

    @Override
    public void activateJob(String jobId) {
        try {
            SuspensionStateDto suspensionStateDto = new SuspensionStateDto()
                    .suspended(false);
            jobApiClient.updateJobSuspensionState(jobId, suspensionStateDto);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable activate job by id {} because of connection error: ", jobId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public void suspendJob(String jobId) {
        try {
            SuspensionStateDto suspensionStateDto = new SuspensionStateDto()
                    .suspended(true);
            jobApiClient.updateJobSuspensionState(jobId, suspensionStateDto);
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (isConnectionError(rootCause)) {
                log.error("Unable suspend job by id {} because of connection error: ", jobId, e);
                throw new EngineConnectionFailedException(e.getMessage(), -1, e.getMessage());
            }
            throw e;
        }
    }

    protected JobQueryDto createJobQueryDto(JobFilter filter) {
        JobQueryDto jobQueryDto = new JobQueryDto();
        String tenantId = engineTenantProvider.getCurrentUserTenantId();
        if (tenantId != null) {
            jobQueryDto.tenantIdIn(List.of(tenantId));
        }
        if (filter != null) {
            jobQueryDto.processInstanceId(filter.getProcessInstanceId());
        }

        return jobQueryDto;
    }

    @Nullable
    protected List<JobQueryDtoSortingInner> createSortOptions(Sort sort) {
        if (sort == null) {
            return null;
        }
        List<JobQueryDtoSortingInner> jobQueryDtoSortingInners = new ArrayList<>();
        for (Sort.Order order : sort.getOrders()) {
            JobQueryDtoSortingInner sortOption = new JobQueryDtoSortingInner();

            switch (order.getProperty()) {
                case "id" -> sortOption.setSortBy(JobQueryDtoSortingInner.SortByEnum.JOB_ID);
                case "retries" -> sortOption.setSortBy(JobQueryDtoSortingInner.SortByEnum.JOB_RETRIES);
                case "dueDate" -> sortOption.setSortBy(JobQueryDtoSortingInner.SortByEnum.JOB_DUE_DATE);
                case "priority" -> sortOption.setSortBy(JobQueryDtoSortingInner.SortByEnum.JOB_PRIORITY);
                default -> {
                }
            }

            if (order.getDirection() == Sort.Direction.ASC) {
                sortOption.setSortOrder(JobQueryDtoSortingInner.SortOrderEnum.ASC);
            } else if (order.getDirection() == Sort.Direction.DESC) {
                sortOption.setSortOrder(JobQueryDtoSortingInner.SortOrderEnum.DESC);
            }
            if (sortOption.getSortBy() != null && sortOption.getSortOrder() != null) {
                jobQueryDtoSortingInners.add(sortOption);
            }
        }

        return jobQueryDtoSortingInners;
    }
}
