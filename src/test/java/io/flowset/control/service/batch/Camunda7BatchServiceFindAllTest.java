/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.batch;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.entity.batch.BatchStatisticsData;
import io.flowset.control.entity.filter.BatchFilter;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.BatchDto;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7BatchServiceFindAllTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    BatchService batchService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("Load all runtime batch statistics with empty load context")
    void givenRuntimeBatchesAndEmptyContext_whenFindAllBatchStatistics_thenAllRuntimeBatchesReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);

        String batchId = batchDto.getId();
        BatchLoadContext loadContext = new BatchLoadContext();

        //when
        List<BatchStatisticsData> statistics = batchService.findAllBatchStatistics(loadContext);

        //then
        assertThat(statistics)
                .extracting(BatchStatisticsData::getId)
                .contains(batchId);
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Load a page with runtime batch statistics")
    void givenRuntimeBatchesAndPagination_whenFindAllBatchStatistics_thenPageReturned(int firstResult, int maxResults,
                                                                                       int maxExpectedCount) {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        // create 3 batches
        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);

        BatchLoadContext loadContext = new BatchLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<BatchStatisticsData> statistics = batchService.findAllBatchStatistics(loadContext);

        //then
        assertThat(statistics).hasSizeLessThanOrEqualTo(maxExpectedCount);
    }

    @Test
    @DisplayName("Load runtime batch statistics by filter with batch id")
    void givenRuntimeBatchAndFilterByBatchId_whenFindAllBatchStatistics_thenFilteredRuntimeBatchReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        BatchFilter filter = dataManager.create(BatchFilter.class);
        filter.setBatchId(batchId);

        BatchLoadContext loadContext = new BatchLoadContext()
                .setFilter(filter);

        //when
        List<BatchStatisticsData> statistics = new ArrayList<>();
        for (int attempt = 0; attempt < 20; attempt++) {
            statistics = batchService.findAllBatchStatistics(loadContext);
            if (!statistics.isEmpty()) {
                break;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        //then
        assertThat(statistics)
                .hasSizeLessThanOrEqualTo(1)
                .allSatisfy(batchStatistics -> assertThat(batchStatistics.getId()).isEqualTo(batchId));
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when load runtime batch statistics if engine is not available")
    void givenRuntimeBatchesAndNotAvailableEngine_whenFindAllBatchStatistics_thenExceptionThrown() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);

        BatchLoadContext loadContext = new BatchLoadContext();
        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> batchService.findAllBatchStatistics(loadContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Load all historic batches with empty load context")
    void givenCompletedBatchesAndEmptyContext_whenFindAllHistoricBatches_thenAllHistoricBatchesReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();
        camundaRestTestHelper.waitForBatchExecution(camunda7);

        BatchLoadContext loadContext = new BatchLoadContext();

        //when
        List<BatchData> batches = batchService.findAllHistoricBatches(loadContext);

        //then
        assertThat(batches)
                .extracting(BatchData::getId)
                .contains(batchId);
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Load a page with historic batches")
    void givenCompletedBatchesAndPagination_whenFindAllHistoricBatches_thenPageReturned(int firstResult, int maxResults,
                                                                                         int expectedCount) {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        // create 3 batches
        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);

        camundaRestTestHelper.waitForBatchExecution(camunda7);

        BatchLoadContext loadContext = new BatchLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<BatchData> batches = batchService.findAllHistoricBatches(loadContext);

        //then
        assertThat(batches).hasSize(expectedCount);
    }

    @Test
    @DisplayName("Load historic batches by filter with batch id and completed flag")
    void givenCompletedBatchAndFilterByBatchId_whenFindAllHistoricBatches_thenFilteredHistoricBatchReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();
        camundaRestTestHelper.waitForBatchExecution(camunda7);

        BatchFilter filter = dataManager.create(BatchFilter.class);
        filter.setBatchId(batchId);
        filter.setCompleted(true);

        BatchLoadContext loadContext = new BatchLoadContext()
                .setFilter(filter);

        //when
        List<BatchData> batches = batchService.findAllHistoricBatches(loadContext);

        //then
        assertThat(batches)
                .hasSize(1)
                .first()
                .satisfies(batchData -> assertThat(batchData.getId()).isEqualTo(batchId));
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when load historic batches if engine is not available")
    void givenCompletedBatchesAndNotAvailableEngine_whenFindAllHistoricBatches_thenExceptionThrown() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        camundaRestTestHelper.waitForBatchExecution(camunda7);

        BatchLoadContext loadContext = new BatchLoadContext();
        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> batchService.findAllHistoricBatches(loadContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    static Stream<Arguments> provideValidPaginationData() {
        return Stream.of(
                Arguments.of(0, 2, 2),
                Arguments.of(2, 4, 1)
        );
    }

}

