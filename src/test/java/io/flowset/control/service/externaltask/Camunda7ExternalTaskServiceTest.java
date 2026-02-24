/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.externaltask;

import feign.FeignException;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.jmix.core.DataManager;
import io.flowset.control.entity.filter.ExternalTaskFilter;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.HandleFailureDto;
import io.flowset.control.test_support.camunda7.dto.response.ExternalTaskDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@Slf4j
@WithRunningEngine
public class Camunda7ExternalTaskServiceTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ExternalTaskService externalTaskService;

    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("Set non-zero retries for active external task")
    void givenActiveJobWithZeroRetries_whenSetRetries_thenRetriesUpdated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTaskRetriesUpdate.bpmn")
                .startByKey("testExternalTaskRetriesUpdate");

        List<String> instanceIds = sampleDataManager.getStartedInstances("testExternalTaskRetriesUpdate");

        ExternalTaskDto sourceTaskDto = camundaRestTestHelper.getExternalTasks(camunda7, instanceIds).get(0);
        String externalTaskId = sourceTaskDto.getId();

        //when
        externalTaskService.setRetries(externalTaskId, 5);

        //then
        ExternalTaskDto updatedTaskDto = camundaRestTestHelper.getExternalTaskById(camunda7, sourceTaskDto.getId());
        assertThat(updatedTaskDto).isNotNull();
        assertThat(updatedTaskDto.getRetries()).isEqualTo(5);
        assertThat(updatedTaskDto.getRetries()).isNotEqualTo(sourceTaskDto.getRetries());
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when set external task retries if engine is not available")
    void givenExistingTaskAndNotAvailableEngine_whenSetRetries_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTaskRetriesUpdate.bpmn")
                .startByKey("testExternalTaskRetriesUpdate");

        List<String> instanceIds = sampleDataManager.getStartedInstances("testExternalTaskRetriesUpdate");

        ExternalTaskDto sourceTaskDto = camundaRestTestHelper.getExternalTasks(camunda7, instanceIds).get(0);
        String externalTaskId = sourceTaskDto.getId();

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> externalTaskService.setRetries(externalTaskId, 5))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Set non-zero retries async for active external task")
    void givenActiveExternalTasksWithZeroRetries_whenSetRetriesAsync_thenRetriesUpdated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTaskRetriesUpdate.bpmn")
                .startByKey("testExternalTaskRetriesUpdate", 2);

        List<String> instanceIds = sampleDataManager.getStartedInstances("testJobRetriesUpdate");

        List<String> taskIds = camundaRestTestHelper.getExternalTaskIds(camunda7, instanceIds);

        //when
        externalTaskService.setRetriesAsync(taskIds, 5);
        waitForBatchExecution();

        //then
        List<ExternalTaskDto> updatedTasks = camundaRestTestHelper.getExternalTasksByIds(camunda7, taskIds);
        assertThat(updatedTasks)
                .hasSize(2)
                .allSatisfy(externalTaskDto -> {
                    assertThat(externalTaskDto.getRetries()).isEqualTo(5);
                    assertThat(externalTaskDto.getId()).isIn(taskIds);
                });
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when set external task retries async if engine is not available")
    void givenExistingExternalTaskAndNotAvailableEngine_whenSetRetriesAsync_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTaskRetriesUpdate.bpmn")
                .startByKey("testExternalTaskRetriesUpdate", 2);

        List<String> instanceIds = sampleDataManager.getStartedInstances("testJobRetriesUpdate");

        List<String> taskIds = camundaRestTestHelper.getExternalTaskIds(camunda7, instanceIds);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> externalTaskService.setRetriesAsync(taskIds, 5))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get count of all external tasks if filter is null")
    void givenExternalTasks_whenGetRunningTasksCount_thenAllTasksCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testExternalTasksListLoad.bpmn")
                .startByKey("testExternalTasksListLoad", 2);

        //when
        long tasksCount = externalTaskService.getRunningTasksCount(null);

        //then
        assertThat(tasksCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Get count of external tasks by process instance id")
    void givenExternalTasksAndFilterWithProcessInstanceId_whenGetRunningTasksCount_thenTasksCountForInstanceReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTasksListLoad.bpmn")
                .startByKey("testExternalTasksListLoad", 2);

        String instanceId = sampleDataManager.getStartedInstances("testExternalTasksListLoad").get(0);

        ExternalTaskFilter filter = dataManager.create(ExternalTaskFilter.class);
        filter.setProcessInstanceId(instanceId);

        //when
        long tasksCount = externalTaskService.getRunningTasksCount(filter);

        //then
        assertThat(tasksCount).isEqualTo(1);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get runtime tasks count if engine is not available")
    void givenExistingTasksAndNotAvailableEngine_whenGetRunningTasksCount_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTasksListLoad.bpmn")
                .startByKey("testExternalTasksListLoad", 2);

        String instanceId = sampleDataManager.getStartedInstances("testExternalTasksListLoad").get(0);

        ExternalTaskFilter filter = dataManager.create(ExternalTaskFilter.class);
        filter.setProcessInstanceId(instanceId);

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> externalTaskService.getRunningTasksCount(filter))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get error details of active external task by existing task id")
    void givenExistingExternalTaskWithError_whenGetErrorDetails_thenErrorIsReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");

        List<String> instanceIds = sampleDataManager.getStartedInstances("testFailedExternalTask");
        String taskId = camundaRestTestHelper.getExternalTaskIds(camunda7, instanceIds).get(0);

        camundaRestTestHelper.failExternalTask(camunda7, taskId, HandleFailureDto.builder()
                .errorMessage("Service not available")
                .errorDetails("I/O exception occurred during service connection")
                .retries(0)
                .workerId("testWorker")
                .build());

        //when
        String errorDetails = externalTaskService.getErrorDetails(taskId);

        //then
        assertThat(errorDetails).isEqualTo("I/O exception occurred during service connection");
    }

    @Test
    @DisplayName("Get error details of active external task by non-existing task id")
    void givenNonExistingExternalTaskId_whenGetErrorDetails_thenExceptionThrown() {
        //given
        String nonExistingTaskId = UUID.randomUUID().toString();

        //when and then
        assertThatThrownBy(() -> externalTaskService.getErrorDetails(nonExistingTaskId))
                .isInstanceOf(RemoteProcessEngineException.class);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get error details if engine is not available")
    void givenExternalTaskWithErrorAndNotAvailableEngine_whenGetErrorDetails_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");

        List<String> instanceIds = sampleDataManager.getStartedInstances("testFailedExternalTask");
        String taskId = camundaRestTestHelper.getExternalTaskIds(camunda7, instanceIds).get(0);

        camundaRestTestHelper.failExternalTask(camunda7, taskId, HandleFailureDto.builder()
                .errorMessage("Service not available")
                .errorDetails("I/O exception occurred during service connection")
                .retries(0)
                .workerId("testWorker")
                .build());

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> externalTaskService.getErrorDetails(taskId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get null error details for external task without errors")
    void givenExistingActiveExternalTaskWithoutError_whenGetErrorDetails_thenNullIsReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");

        List<String> instanceIds = sampleDataManager.getStartedInstances("testFailedExternalTask");
        String taskId = camundaRestTestHelper.getExternalTaskIds(camunda7, instanceIds).get(0);

        //when
        String errorDetails = externalTaskService.getErrorDetails(taskId);

        //then
        assertThat(errorDetails).isNull();
    }

    @Test
    @DisplayName("Find existing external task by id")
    void givenActiveExternalTask_whenFindById_thenExternalTaskReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");

        List<String> instanceIds = sampleDataManager.getStartedInstances("testFailedExternalTask");
        String externalTaskId = camundaRestTestHelper.getExternalTaskIds(camunda7, instanceIds).get(0);

        //when
        ExternalTaskData foundExternalTask = externalTaskService.findById(externalTaskId);

        //then
        assertThat(foundExternalTask).isNotNull();
        assertThat(foundExternalTask.getId()).isEqualTo(externalTaskId);
        assertThat(foundExternalTask.getTopicName()).isEqualTo("failed-external-task-topic");
        assertThat(foundExternalTask.getProcessInstanceId()).isEqualTo(instanceIds.get(0));
        assertThat(foundExternalTask.getActivityId()).isEqualTo("failedExternalTask");
    }

    @Test
    @DisplayName("Find external task by non-existing id")
    void givenNonExistingExternalTaskId_whenFindById_thenExceptionThrown() {
        //given
        String externalTaskId = "non-existing-external-task-id";

        //when and then
        assertThatThrownBy(() -> externalTaskService.findById(externalTaskId))
                .isInstanceOf(FeignException.class)
                .satisfies(throwable -> {
                    FeignException feignException = (FeignException) throwable;
                    assertThat(feignException.status()).isEqualTo(404);
                });
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find existing external task by id if engine is not available")
    void givenActiveExternalTaskAndNotAvailableEngine_whenFindById_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testFailedExternalTask.bpmn")
                .startByKey("testFailedExternalTask");

        List<String> instanceIds = sampleDataManager.getStartedInstances("testFailedExternalTask");
        String externalTaskId = camundaRestTestHelper.getExternalTaskIds(camunda7, instanceIds).get(0);

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> externalTaskService.findById(externalTaskId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }


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
