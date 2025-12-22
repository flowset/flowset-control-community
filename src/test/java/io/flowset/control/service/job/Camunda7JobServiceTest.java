/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.job;

import io.jmix.core.DataManager;
import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobDefinitionData;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.JobDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7JobServiceTest extends AbstractCamunda7IntegrationTest {

    @Autowired
    private CamundaRestTestHelper camundaRestTestHelper;

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    JobService jobService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("Set non-zero retries for active job")
    void givenActiveJobWithZeroRetries_whenSetRetries_thenRetriesUpdated() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobRetriesUpdate.bpmn")
                .startByKey("testJobRetriesUpdate");

        JobDto sourceJobDto = camundaRestTestHelper.getJobsByProcessKey(camunda7, "testJobRetriesUpdate").get(0);
        String jobId = sourceJobDto.getId();

        //when
        jobService.setJobRetries(jobId, 5);

        //then
        JobDto updatedJobDto = camundaRestTestHelper.getJobById(camunda7, sourceJobDto.getId());
        assertThat(updatedJobDto).isNotNull();
        assertThat(updatedJobDto.getRetries()).isEqualTo(5);
        assertThat(updatedJobDto.getRetries()).isNotEqualTo(sourceJobDto.getRetries());
    }

    @Test
    @DisplayName("Set non-zero retries async for active jobs")
    void givenActiveJobsWithZeroRetries_whenSetRetriesAsync_thenRetriesUpdated() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobRetriesUpdate.bpmn")
                .startByKey("testJobRetriesUpdate", 2);

        List<String> jobIds = camundaRestTestHelper.getJobIdsByProcessKey(camunda7, "testJobRetriesUpdate");

        //when
        jobService.setJobRetriesAsync(jobIds, 5);
        waitForBatchExecution();

        //then
        List<JobDto> updatedJobs = camundaRestTestHelper.getJobsByIds(camunda7, jobIds);
        assertThat(updatedJobs)
                .hasSize(2)
                .allSatisfy(jobDto -> {
                    assertThat(jobDto.getRetries()).isEqualTo(5);
                    assertThat(jobDto.getId()).isIn(jobIds);
                });
    }


    @Test
    @DisplayName("Find job definition by existing id")
    void givenActiveJob_whenFindJobDefinition_thenJobDefinitionReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob");

        JobDto sourceJobDto = camundaRestTestHelper.getJobsByProcessKey(camunda7, "testTimerJob").get(0);
        String jobDefinitionId = sourceJobDto.getJobDefinitionId();

        //when
        JobDefinitionData jobDefinition = jobService.findJobDefinition(jobDefinitionId);

        //then
        assertThat(jobDefinition).isNotNull();
        assertThat(jobDefinition.getJobDefinitionId()).isEqualTo(jobDefinitionId);
        assertThat(jobDefinition.getJobType()).isEqualTo("timer-intermediate-transition");
        assertThat(jobDefinition.getActivityId()).isEqualTo("timerEvent");
    }

    @Test
    @DisplayName("Find job by existing id")
    void givenActiveJob_whenFindJob_thenJobReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob");

        JobDto sourceJobDto = camundaRestTestHelper.getJobsByProcessKey(camunda7, "testTimerJob").get(0);
        String jobId = sourceJobDto.getId();

        //when
        JobData foundJob = jobService.findById(jobId);

        //then
        assertThat(foundJob).isNotNull();
        assertThat(foundJob.getId()).isEqualTo(jobId);
        assertThat(foundJob.getRetries()).isEqualTo(3);
        assertThat(foundJob.getJobDefinitionId()).isEqualTo(sourceJobDto.getJobDefinitionId());

    }

    @Test
    @DisplayName("Get count of all jobs if filter is null")
    void givenActiveJobs_whenGetCount_thenAllJobsCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob", 2);

        //when
        long jobsCount = jobService.getCount(null);

        //then
        assertThat(jobsCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Get count of jobs by process instance id")
    void givenActiveJobsAndFilterWithProcessInstanceId_whenGetCount_thenJobsCountForInstanceReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob", 2);

        String instanceId = sampleDataManager.getStartedInstances("testTimerJob").get(0);

        JobFilter jobFilter = dataManager.create(JobFilter.class);
        jobFilter.setProcessInstanceId(instanceId);

        //when
        long jobsCount = jobService.getCount(jobFilter);

        //then
        assertThat(jobsCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Get error details of active job by existing job id")
    void givenExistingActiveJobWithError_whenGetErrorDetails_thenErrorIsReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .waitJobsExecution();

        List<String> instanceIds = sampleDataManager.getStartedInstances("testFailedJobIncident");
        JobDto failedJob = camundaRestTestHelper.getFailedJobs(camunda7, instanceIds).get(0);

        //when
        String errorDetails = jobService.getErrorDetails(failedJob.getId());

        //then
        assertThat(errorDetails).isNotNull()
                .contains("Some service not available");
    }

    @Test
    @DisplayName("Get empty error details for active job without errors")
    void givenExistingActiveJobWithoutError_whenGetErrorDetails_thenEmptyStringIsReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob");

        String jobId = camundaRestTestHelper.getJobIdsByProcessKey(camunda7, "testTimerJob").get(0);

        //when
        String errorDetails = jobService.getErrorDetails(jobId);

        //then
        assertThat(errorDetails).isEmpty();
    }

    @Test
    @DisplayName("Suspend active existing job by id")
    void givenExistingActiveJob_whenSuspendById_thenJobSuspended() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob");

        String jobId = camundaRestTestHelper.getJobIdsByProcessKey(camunda7, "testTimerJob").get(0);

        //when
        jobService.suspendJob(jobId);

        //then
        JobDto updatedJob = camundaRestTestHelper.getJobById(camunda7, jobId);
        assertThat(updatedJob).isNotNull();
        assertThat(updatedJob.getSuspended()).isTrue();
    }

    @Test
    @DisplayName("Activate suspended existing job by id")
    void givenExistingSuspendedJob_whenActivateById_thenJobActivated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob");

        String jobId = camundaRestTestHelper.getJobIdsByProcessKey(camunda7, "testTimerJob").get(0);
        camundaRestTestHelper.suspendJobById(camunda7, jobId);

        //when
        jobService.activateJob(jobId);

        //then
        JobDto updatedJob = camundaRestTestHelper.getJobById(camunda7, jobId);
        assertThat(updatedJob).isNotNull();
        assertThat(updatedJob.getSuspended()).isFalse();
    }

    /*
    TODO: fix
    @Test
    @DisplayName("Get history error details of active job by existing job id")
    void givenExistingActiveJobWithError_whenGetHistoryErrorDetails_thenErrorIsReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution();

        List<String> instanceIds = sampleDataManager.getStartedInstances("testFailedJobIncident");
        JobDto failedJob = camundaRestTestHelper.getFailedJobs(camunda7, instanceIds).get(0);

        //when
        String errorDetails = jobService.getHistoryErrorDetails(failedJob.getId());

        //then
        assertThat(errorDetails).isNotNull()
                .contains("Some service not available");
    }*/

    /*TODO Fix
    @Test
    @DisplayName("Get history error details of completed job by existing job")
    void givenExistingCompletedJobWithError_whenGetHistoryErrorDetails_thenErrorIsReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs()
                .waitJobsExecution();

        List<String> instanceIds = sampleDataManager.getStartedInstances("testResolvedFailedJobIncident");
        List<HistoricIncidentDto> incidents = camundaRestTestHelper.findHistoricIncidentsByInstanceId(camunda7, instanceIds.get(0));

        String jobId = null;


        //when
        String errorDetails = jobService.getHistoryErrorDetails("jobLogId");

        //then
        assertThat(errorDetails).isNotNull()
                .contains("Some service not available");
    }*/


    private void waitForBatchExecution() {
        boolean batchExists;
        int attempts = 0;
        do {
            batchExists = camundaRestTestHelper.activeBatchExits(camunda7);
            attempts++;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            if (attempts > 100) { //prevent infinite loop
                break;
            }
        } while (batchExists);
    }
}
