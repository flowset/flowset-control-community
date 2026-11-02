/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.job;

import io.flowset.control.entity.job.JobData;
import io.flowset.control.entity.job.JobDefinitionData;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class NoopJobServiceTest extends AbstractIntegrationTest {

    @Autowired
    JobService jobService;

    @Test
    @DisplayName("Empty list of jobs returned when no engine exists")
    void givenNoEngine_whenFindAll_thenEmptyListReturned() {
        //when
        List<JobData> jobs = jobService.findAll(new JobLoadContext());

        //then
        assertThat(jobs).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as jobs count when no engine exists")
    void givenNoEngine_whenGetCount_thenZeroReturned() {
        //when
        long count = jobService.getCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for job when no engine exists")
    void givenNoEngine_whenFindById_thenNullReturned() {
        //when
        JobData job = jobService.findById("jobId");

        //then
        assertThat(job).isNull();
    }

    @Test
    @DisplayName("Null returned for job definition when no engine exists")
    void givenNoEngine_whenFindJobDefinition_thenNullReturned() {
        //when
        JobDefinitionData jobDefinition = jobService.findJobDefinition("jobDefinitionId");

        //then
        assertThat(jobDefinition).isNull();
    }

    @Test
    @DisplayName("Null returned as job error details when no engine exists")
    void givenNoEngine_whenGetErrorDetails_thenNullReturned() {
        //when
        String errorDetails = jobService.getErrorDetails("jobId");

        //then
        assertThat(errorDetails).isNull();
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when set job retries if no engine exists")
    void givenNoEngine_whenSetJobRetries_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> jobService.setJobRetries("jobId", 1))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when set job retries async if no engine exists")
    void givenNoEngine_whenSetJobRetriesAsync_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> jobService.setJobRetriesAsync(List.of("jobId"), 1))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when activate job if no engine exists")
    void givenNoEngine_whenActivateJob_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> jobService.activateJob("jobId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when suspend job if no engine exists")
    void givenNoEngine_whenSuspendJob_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> jobService.suspendJob("jobId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }
}
