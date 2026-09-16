/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.activity;

import io.flowset.control.entity.activity.ActivityInstanceTreeItem;
import io.flowset.control.entity.activity.ProcessActivityStatistics;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.VariableValueDto;
import io.flowset.control.test_support.camunda7.dto.response.ExecutionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ActivityServiceTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    ActivityService activityService;
    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Load activity statistics by process definition id")
    void givenProcessWithRunningInstances_whenGetStatisticsByProcessId_thenStatisticsReturned() {
        // given
        StartProcessDto startProcessDto = new StartProcessDto();
        startProcessDto.setVariables(Map.of("fail",
                new VariableValueDto("boolean", true)));

        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testActivityStatistics.bpmn")
                .startByKey("testActivityStatistics", 3)
                .startByKey("testActivityStatistics", startProcessDto, 2)
                .waitJobsExecution();

        String processDefId = sampleDataManager.getDeployedProcessVersions("testActivityStatistics").get(0);

        // when
        List<ProcessActivityStatistics> stats = activityService.getStatisticsByProcessId(processDefId);

        // then
        assertThat(stats).isNotEmpty()
                .hasSize(2);

        assertThat(stats)
                .filteredOn("activityId", "testScriptTask")
                .singleElement()
                .satisfies(stat -> {
                    assertThat(stat.getInstanceCount()).isEqualTo(2);
                    assertThat(stat.getFailedJobCount()).isEqualTo(2);
                    assertThat(stat.getIncidents())
                            .hasSize(1)
                            .extracting("incidentType", "incidentCount")
                            .contains(tuple("failedJob", 2));
                });

        assertThat(stats)
                .filteredOn("activityId", "testUserTask")
                .singleElement()
                .satisfies(stat -> {
                    assertThat(stat.getInstanceCount()).isEqualTo(3);
                    assertThat(stat.getFailedJobCount()).isZero();
                    assertThat(stat.getIncidents()).isNullOrEmpty();
                });
    }

    @Test
    @DisplayName("Failed job inside sub-process has sub-process execution id")
    void givenFailedJobInSubProcess_whenLoadingActivityTree_thenItHasSubProcessExecutionId() {
        // given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testTransitionInstanceSubProcess.bpmn")
                .startByKey("testTransitionInstanceSubProcess")
                .waitJobsExecution();

        String processInstanceId = sampleDataManager.getStartedInstances("testTransitionInstanceSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        // when
        List<ActivityInstanceTreeItem> treeItems = activityService.getActivityInstancesTree(processInstanceId);

        // then
        assertThat(treeItems)
                .filteredOn(ActivityInstanceTreeItem::getTransition)
                .singleElement()
                .extracting(ActivityInstanceTreeItem::getActivityId, treeItem ->
                        treeItem.getExecutionIds().getFirst())
                .containsExactly("subFailingTask", subProcessExecutionId);
    }

    @Test
    @DisplayName("Activity inside sub-process has sub-process execution id")
    void givenInstanceInSubProcess_whenLoadingActivityTree_thenActivityHasSubProcessExecutionId() {
        // given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = sampleDataManager.getStartedInstances("testLocalVariableSubProcess").getFirst();
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        // when
        List<ActivityInstanceTreeItem> treeItems = activityService.getActivityInstancesTree(processInstanceId);

        // then
        assertThat(subProcessExecutionId).isNotEqualTo(processInstanceId);

        assertThat(treeItems)
                .filteredOn(treeItem -> treeItem.getParentActivityInstance() == null)
                .singleElement()
                .extracting(treeItem -> treeItem.getExecutionIds().getFirst())
                .isEqualTo(processInstanceId);

        assertThat(treeItems)
                .filteredOn(treeItem -> treeItem.getParentActivityInstance() != null)
                .extracting(ActivityInstanceTreeItem::getActivityId, treeItem ->
                        treeItem.getExecutionIds().getFirst())
                .containsExactlyInAnyOrder(
                        tuple("subProcess", subProcessExecutionId),
                        tuple("subUserTask", subProcessExecutionId));
    }

    String findSubProcessExecutionId(String processInstanceId) {
        return camundaRestTestHelper.findExecutions(camunda7, processInstanceId)
                .stream()
                .map(ExecutionDto::getId)
                .filter(executionId -> !executionId.equals(processInstanceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No child execution found for " + processInstanceId));
    }
}
