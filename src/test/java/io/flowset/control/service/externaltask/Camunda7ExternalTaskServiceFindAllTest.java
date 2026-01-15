/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.externaltask;

import io.flowset.control.exception.EngineConnectionFailedException;
import io.jmix.core.DataManager;
import io.flowset.control.entity.ExternalTaskData;
import io.flowset.control.entity.filter.ExternalTaskFilter;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import lombok.extern.slf4j.Slf4j;
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

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@Slf4j
@WithRunningEngine
public class Camunda7ExternalTaskServiceFindAllTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ExternalTaskService externalTaskService;

    @Autowired
    DataManager dataManager;


    @Test
    @DisplayName("Load all external tasks with empty load context")
    void givenActiveExternalTasksAndEmptyContext_whenFindRunningTasks_thenAllTasksReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTasksListLoad.bpmn")
                .startByKey("testExternalTasksListLoad", 3);

        List<String> startedInstances = sampleDataManager.getStartedInstances("testExternalTasksListLoad");

        ExternalTaskLoadContext loadContext = new ExternalTaskLoadContext();

        //when
        List<ExternalTaskData> runningTasks = externalTaskService.findRunningTasks(loadContext);

        //then
        assertThat(runningTasks)
                .hasSize(3)
                .allSatisfy(
                   externalTaskData -> {
                       assertThat(externalTaskData.getExternalTaskId()).isNotNull();
                       assertThat(externalTaskData.getTopicName()).isEqualTo("test-external-task-topic");
                       assertThat(externalTaskData.getProcessDefinitionKey()).isEqualTo("testExternalTasksListLoad");
                       assertThat(externalTaskData.getProcessInstanceId()).isIn(startedInstances);
                   }
                );
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Load a page with external tasks")
    void givenActiveExternalTasksAndContextWithPagination_whenFindRunningTasks_thenPageWithTasksReturned(int firstResult, int maxResults,
                                                                                                         int expectedCount) {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTasksListLoad.bpmn")
                .startByKey("testExternalTasksListLoad", 3);

        ExternalTaskLoadContext loadContext = new ExternalTaskLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<ExternalTaskData> runningTasks = externalTaskService.findRunningTasks(loadContext);

        //then
        assertThat(runningTasks).hasSize(expectedCount);
    }

    @Test
    @DisplayName("All external tasks for process instance returned if context has a filter by process instance id")
    void givenExternalTasksAndContextWithFilter_whenFindRunningTasksByProcessInstanceId_thenAllTasksForInstanceReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTasksListLoad.bpmn")
                .startByKey("testExternalTasksListLoad", 2);

        String instanceId = sampleDataManager.getStartedInstances("testExternalTasksListLoad").get(0);

        ExternalTaskFilter filter = dataManager.create(ExternalTaskFilter.class);
        filter.setProcessInstanceId(instanceId);

        ExternalTaskLoadContext loadContext = new ExternalTaskLoadContext()
                .setFilter(filter);
        //when
        List<ExternalTaskData> runningTasks = externalTaskService.findRunningTasks(loadContext);

        //then
        assertThat(runningTasks)
                .hasSize(1)
                .first()
                .satisfies(externalTaskData -> {
                    assertThat(externalTaskData.getProcessInstanceId()).isEqualTo(instanceId);
                    assertThat(externalTaskData.getProcessDefinitionKey()).isEqualTo("testExternalTasksListLoad");
                });
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find all external tasks if engine is not available")
    void givenExistingExternalTasksAndNotAvailableEngine_whenFindRunningTasks_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager
                .deploy("test_support/testExternalTasksListLoad.bpmn")
                .startByKey("testExternalTasksListLoad", 3);

        ExternalTaskLoadContext loadContext = new ExternalTaskLoadContext();

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> externalTaskService.findRunningTasks(loadContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    static Stream<Arguments> provideValidPaginationData() {
        return Stream.of(
                Arguments.of(0, 2, 2),
                Arguments.of(2, 4, 1)
        );
    }
}
