/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.job;

import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.JobDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.flowset.control.test_support.security.role.job.TestJobActivatePermissionRole;
import io.flowset.control.test_support.security.role.job.TestJobRetryPermissionRole;
import io.flowset.control.test_support.security.role.job.TestJobSuspendPermissionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRunningEngine
public class Camunda7JobServiceSecurityTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    JobService jobService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Set job retries when user has retry permission")
    @WithTestUser(username = "test-user-secured-job-retry", roles = TestJobRetryPermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-job-retry")
    void givenUserWithJobRetryPermission_whenSetJobRetries_thenRetriesUpdated() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobRetriesUpdate.bpmn")
                .startByKey("testJobRetriesUpdate");
        JobDto sourceJob = camundaRestTestHelper.getJobsByProcessKey(camunda7, "testJobRetriesUpdate").get(0);

        //when
        jobService.setJobRetries(sourceJob.getId(), 5);

        //then
        JobDto updatedJob = camundaRestTestHelper.getJobById(camunda7, sourceJob.getId());
        assertThat(updatedJob).isNotNull();
        assertThat(updatedJob.getRetries()).isEqualTo(5);
    }

    @Test
    @DisplayName("Activate job when user has activate permission")
    @WithTestUser(username = "test-user-secured-job-activate", roles = TestJobActivatePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-job-activate")
    void givenUserWithJobActivatePermission_whenActivateJob_thenJobActivated() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testTimerJob.bpmn")
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

    @Test
    @DisplayName("Suspend job when user has suspend permission")
    @WithTestUser(username = "test-user-secured-job-suspend", roles = TestJobSuspendPermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-job-suspend")
    void givenUserWithJobSuspendPermission_whenSuspendJob_thenJobSuspended() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob");
        String jobId = camundaRestTestHelper.getJobIdsByProcessKey(camunda7, "testTimerJob").get(0);

        //when
        jobService.suspendJob(jobId);

        //then
        JobDto updatedJob = camundaRestTestHelper.getJobById(camunda7, jobId);
        assertThat(updatedJob).isNotNull();
        assertThat(updatedJob.getSuspended()).isTrue();
    }
}
