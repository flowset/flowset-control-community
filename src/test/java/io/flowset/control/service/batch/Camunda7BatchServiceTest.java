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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7BatchServiceTest extends AbstractCamunda7IntegrationTest {

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
    @DisplayName("Find runtime batch statistics by existing id")
    void givenRuntimeBatchId_whenFindBatchStatisticsById_thenBatchStatisticsReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        //when
        BatchStatisticsData batchStatistics = batchService.findBatchStatisticsById(batchId);

        //then
        assertThat(batchStatistics).isNotNull();
        assertThat(batchStatistics.getId()).isEqualTo(batchId);
        assertThat(batchStatistics.getType()).isEqualTo("instance-update-suspension-state");
    }

    @Test
    @DisplayName("Find runtime batch statistics by non-existing id")
    void givenNonExistingBatchId_whenFindBatchStatisticsById_thenNullReturned() {
        //given
        String batchId = "non-existing-batch-id";

        //when
        BatchStatisticsData batchStatistics = batchService.findBatchStatisticsById(batchId);

        //then
        assertThat(batchStatistics).isNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find runtime batch statistics by id if engine is not available")
    void givenBatchIdAndNotAvailableEngine_whenFindBatchStatisticsById_thenExceptionThrown() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> batchService.findBatchStatisticsById(batchId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Find runtime batch by id")
    void givenRuntimeBatchId_whenGetById_thenRuntimeBatchReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        //when
        BatchData batchData = batchService.getById(batchId);

        //then
        assertThat(batchData).isNotNull();
        assertThat(batchData.getId()).isEqualTo(batchId);
        assertThat(batchData.getEndTime()).isNull();
        assertThat(batchData.getType()).isEqualTo("instance-update-suspension-state");
    }

    @Test
    @DisplayName("Find completed batch by id")
    void givenCompletedBatchId_whenGetById_thenHistoricBatchReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        camundaRestTestHelper.waitForBatchExecution(camunda7);

        //when
        BatchData batchData = batchService.getById(batchId);

        //then
        assertThat(batchData).isNotNull();
        assertThat(batchData.getId()).isEqualTo(batchId);
        assertThat(batchData.getEndTime()).isNotNull();
        assertThat(batchData.getType()).isEqualTo("instance-update-suspension-state");
    }

    @Test
    @DisplayName("Find batch by non-existing id")
    void givenNonExistingBatchId_whenGetById_thenNullReturned() {
        //given
        String batchId = "non-existing-batch-id";

        //when
        BatchData batchData = batchService.getById(batchId);

        //then
        assertThat(batchData).isNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find batch by id if engine is not available")
    void givenBatchIdAndNotAvailableEngine_whenGetById_thenExceptionThrown() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> batchService.getById(batchId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Find historic batch by existing id")
    void givenCompletedBatchId_whenFindHistoricBatchById_thenHistoricBatchReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        camundaRestTestHelper.waitForBatchExecution(camunda7);

        //when
        BatchData batchData = batchService.findHistoricBatchById(batchId);

        //then
        assertThat(batchData).isNotNull();
        assertThat(batchData.getId()).isEqualTo(batchId);
        assertThat(batchData.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("Find historic batch by non-existing id")
    void givenNonExistingBatchId_whenFindHistoricBatchById_thenNullReturned() {
        //given
        String batchId = "non-existing-batch-id";

        //when
        BatchData batchData = batchService.findHistoricBatchById(batchId);

        //then
        assertThat(batchData).isNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find historic batch by id if engine is not available")
    void givenHistoricBatchIdAndNotAvailableEngine_whenFindHistoricBatchById_thenExceptionThrown() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

       BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        camundaRestTestHelper.waitForBatchExecution(camunda7);
        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> batchService.findHistoricBatchById(batchId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get count of runtime batch statistics with null filter")
    void givenRuntimeBatches_whenGetBatchStatisticsCountWithNullFilter_thenCountReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);

        //when
        long count = batchService.getBatchStatisticsCount(null);

        //then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Get count of runtime batch statistics with filter by batch id")
    void givenRuntimeBatchAndFilterByBatchId_whenGetBatchStatisticsCount_thenFilteredCountReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        BatchDto batchDto = camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);
        String batchId = batchDto.getId();

        BatchFilter filter = dataManager.create(BatchFilter.class);
        filter.setBatchId(batchId);

        //when
        long count = batchService.getBatchStatisticsCount(filter);

        //then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Get count of historic batches with null filter")
    void givenCompletedBatches_whenGetHistoricBatchCountWithNullFilter_thenCountReturned() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);

        camundaRestTestHelper.waitForBatchExecution(camunda7);

        //when
        long count = batchService.getHistoricBatchCount(null);

        //then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Get count of historic batches with filter by batch id")
    void givenCompletedBatchAndFilterByBatchId_whenGetHistoricBatchCount_thenFilteredCountReturned() {
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

        //when
        long count = batchService.getHistoricBatchCount(filter);

        //then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get runtime batch statistics count if engine is not available")
    void givenRuntimeBatchesAndNotAvailableEngine_whenGetBatchStatisticsCount_thenExceptionThrown() {
        //given
        List<String> activeInstanceIds = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        camundaRestTestHelper.suspendInstancesAsync(camunda7, activeInstanceIds);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> batchService.getBatchStatisticsCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get historic batch count if engine is not available")
    void givenCompletedBatchesAndNotAvailableEngine_whenGetHistoricBatchCount_thenExceptionThrown() {
        //given
        List<String> startedInstances = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .getStartedInstances("visitPlanning");

        camundaRestTestHelper.suspendInstancesAsync(camunda7, startedInstances);
        camundaRestTestHelper.waitForBatchExecution(camunda7);
        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> batchService.getHistoricBatchCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

}
