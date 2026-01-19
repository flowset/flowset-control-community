/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.job;

import io.flowset.control.exception.EngineConnectionFailedException;
import io.jmix.core.DataManager;
import io.flowset.control.entity.filter.JobFilter;
import io.flowset.control.entity.job.JobData;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7JobServiceFindAllTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    JobService jobService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("All jobs returned if context is empty")
    void givenActiveJobsAndEmptyContext_whenFindAll_thenAllJobsReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad");

        JobLoadContext loadContext = new JobLoadContext();

        //when
        List<JobData> jobs = jobService.findAll(loadContext);

        //then
        assertThat(jobs)
                .hasSize(2)
                .extracting(JobData::getProcessDefinitionKey, JobData::getPriority)
                .containsExactlyInAnyOrder(
                        tuple("testJobsListLoad", 20L),
                        tuple("testJobsListLoad", 30L)
                );
    }

    @Test
    @DisplayName("All jobs for process instance returned if context has a filter by process instance id")
    void givenActiveJobsAndContextWithFilter_whenFindAllByProcessInstanceId_thenAllJobsForInstanceReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob", 2);

        String instanceId = sampleDataManager.getStartedInstances("testTimerJob").get(0);

        JobFilter jobFilter = dataManager.create(JobFilter.class);
        jobFilter.setProcessInstanceId(instanceId);

        JobLoadContext loadContext = new JobLoadContext()
                .setFilter(jobFilter);
        //when
        List<JobData> jobs = jobService.findAll(loadContext);

        //then
        assertThat(jobs)
                .hasSize(1)
                .first()
                .satisfies(jobData -> {
                    assertThat(jobData.getProcessInstanceId()).isEqualTo(instanceId);
                    assertThat(jobData.getProcessDefinitionKey()).isEqualTo("testTimerJob");
                });
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Load a page with jobs")
    void givenActiveJobsAndContextWithValidPagination_whenFindAll_thenPageWithJobsReturned(int firstResult, int maxResults,
                                                                                           int expectedCount) {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testTimerJob.bpmn")
                .startByKey("testTimerJob", 3);

        JobLoadContext loadContext = new JobLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<JobData> jobs = jobService.findAll(loadContext);

        //then
        assertThat(jobs).hasSize(expectedCount);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find all jobs if engine is not available")
    void givenExistingJobsAndNotAvailableEngine_whenFinAll_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testJobsListLoad.bpmn")
                .startByKey("testJobsListLoad");

        JobLoadContext loadContext = new JobLoadContext();

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> jobService.findAll(loadContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    static Stream<Arguments> provideValidPaginationData() {
        return Stream.of(
                Arguments.of(0, 2, 2),
                Arguments.of(2, 4, 1)
        );
    }
}
