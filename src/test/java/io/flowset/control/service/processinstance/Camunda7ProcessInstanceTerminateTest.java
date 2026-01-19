/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.HistoricProcessInstanceDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeProcessInstanceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ProcessInstanceTerminateTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;


    @Test
    @DisplayName("Terminate existing suspended instance by id")
    void givenExistingSuspendedInstance_whenTerminateById_thenInstanceTerminated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String instanceId = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processId).get(0).getId();

        camundaRestTestHelper.suspendInstanceById(camunda7, instanceId);

        //when
        processInstanceService.terminateById(instanceId);

        //then
        RuntimeProcessInstanceDto foundRuntimeInstance = camundaRestTestHelper.getRuntimeInstanceById(camunda7, instanceId);
        assertThat(foundRuntimeInstance).isNull();

        HistoricProcessInstanceDto foundHistoryInstance = camundaRestTestHelper.getHistoryInstanceById(camunda7, instanceId);
        assertThat(foundHistoryInstance).isNotNull();
        assertThat(foundHistoryInstance.getState()).isEqualTo("EXTERNALLY_TERMINATED");
        assertThat(foundHistoryInstance.getEndTime()).isNotNull();

    }

    @ParameterizedTest
    @ValueSource(strings = {"Remove for testing purpose"})
    @NullSource
    @DisplayName("Terminate asynchronously existing suspended instances by ids")
    void givenExistingSuspendedInstances_whenTerminateByIdsAsyncWithReason_thenInstancesTerminated(String reason) {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2);

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        camundaRestTestHelper.suspendInstanceByProcessId(camunda7, processId);

        List<String> suspendedInstanceIds = camundaRestTestHelper.getSuspendedInstancesByProcessId(camunda7, processId);

        //when
        processInstanceService.terminateByIdsAsync(suspendedInstanceIds, reason);
        waitForBatchExecution();

        //then
        List<RuntimeProcessInstanceDto> foundRuntimeInstances = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processId);
        assertThat(foundRuntimeInstances).isEmpty();

        List<HistoricProcessInstanceDto> foundHistoricInstances = camundaRestTestHelper.getHistoryInstancesById(camunda7, processId);
        assertThat(foundHistoricInstances)
                .hasSize(2)
                .allSatisfy(foundHistoricInstance -> {
                    assertThat(foundHistoricInstance.getId())
                            .isIn(suspendedInstanceIds);

                    assertThat(foundHistoricInstance.getState()).isEqualTo("EXTERNALLY_TERMINATED");
                    assertThat(foundHistoricInstance.getEndTime()).isNotNull();
                    assertThat(foundHistoricInstance.getDeleteReason()).isEqualTo(reason);
                });

    }

    @Test
    @DisplayName("Terminate existing active instance by id")
    void givenExistingActiveInstance_whenTerminateById_thenInstanceTerminated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String instanceId = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processId).get(0).getId();

        //when
        processInstanceService.terminateById(instanceId);

        //then
        RuntimeProcessInstanceDto foundRuntimeInstance = camundaRestTestHelper.getRuntimeInstanceById(camunda7, instanceId);
        assertThat(foundRuntimeInstance).isNull();

        HistoricProcessInstanceDto foundHistoryInstance = camundaRestTestHelper.getHistoryInstanceById(camunda7, instanceId);
        assertThat(foundHistoryInstance).isNotNull();
        assertThat(foundHistoryInstance.getState()).isEqualTo("EXTERNALLY_TERMINATED");
        assertThat(foundHistoryInstance.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when terminate instance by id if engine is not available")
    void givenActiveInstanceAndNotAvailableEngine_whenTerminateById_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        String instanceId = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processId).get(0).getId();

        camunda7.stop();


        //when and then
        assertThatThrownBy(() ->  processInstanceService.terminateById(instanceId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Remove for testing purpose"})
    @NullSource
    @DisplayName("Terminate asynchronously existing active instances by ids")
    void givenExistingActiveInstances_whenTerminateByIdsAsync_thenInstancesTerminated(String reason) {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2);

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        List<String> activeInstanceIds = camundaRestTestHelper.getActiveInstancesByProcessId(camunda7, processId);

        //when
        processInstanceService.terminateByIdsAsync(activeInstanceIds, reason);
        waitForBatchExecution();

        //then
        List<RuntimeProcessInstanceDto> foundRuntimeInstances = camundaRestTestHelper.getRuntimeInstancesById(camunda7, processId);
        assertThat(foundRuntimeInstances).isEmpty();

        List<HistoricProcessInstanceDto> foundHistoricInstances = camundaRestTestHelper.getHistoryInstancesById(camunda7, processId);
        assertThat(foundHistoricInstances)
                .hasSize(2)
                .allSatisfy(foundHistoricInstance -> {
                    assertThat(foundHistoricInstance.getId())
                            .isIn(activeInstanceIds);

                    assertThat(foundHistoricInstance.getState()).isEqualTo("EXTERNALLY_TERMINATED");
                    assertThat(foundHistoricInstance.getEndTime()).isNotNull();
                    assertThat(foundHistoricInstance.getDeleteReason()).isEqualTo(reason);
                });
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when terminate instances async if engine is not available")
    void givenActiveInstancesAndNotAvailableEngine_whenTerminateByIdsAsync_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2);

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        List<String> activeInstanceIds = camundaRestTestHelper.getActiveInstancesByProcessId(camunda7, processId);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> processInstanceService.terminateByIdsAsync(activeInstanceIds, null))
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
                //do nothing
            }
            if (attempts > 100) { //prevent infinite loop
                break;
            }
        } while (batchExists);
    }
}
