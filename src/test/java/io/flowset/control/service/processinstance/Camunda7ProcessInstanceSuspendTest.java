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
import io.flowset.control.test_support.camunda7.dto.response.RuntimeProcessInstanceDto;
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
public class Camunda7ProcessInstanceSuspendTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;


    @Test
    @DisplayName("Suspend existing active instance by id")
    void givenExistingActiveInstance_whenSuspendById_thenInstanceSuspended() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String instanceId = sampleDataManager.getStartedInstances("visitPlanning").get(0);

        //when
        processInstanceService.suspendById(instanceId);

        //then
        RuntimeProcessInstanceDto foundInstance = camundaRestTestHelper.getRuntimeInstanceById(camunda7, instanceId);
        assertThat(foundInstance).isNotNull();
        assertThat(foundInstance.getSuspended()).isTrue();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when suspend instance by id if engine is not available")
    void givenActiveInstanceAndNotAvailableEngine_whenSuspendById_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning");

        String instanceId = sampleDataManager.getStartedInstances("visitPlanning").get(0);

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> processInstanceService.suspendById(instanceId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Suspend asynchronously existing active instances by ids")
    void givenExistingActiveInstances_whenSuspendByIdsAsync_thenInstancesSuspended() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 5);

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        List<String> activeInstanceIds = camundaRestTestHelper.getActiveInstancesByProcessId(camunda7, processId);

        //when
        processInstanceService.suspendByIdsAsync(activeInstanceIds);
        waitForBatchExecution();

        //then
        List<String> suspendedInstanceIds = camundaRestTestHelper.getSuspendedInstancesByProcessId(camunda7, processId);
        assertThat(suspendedInstanceIds)
                .hasSize(5)
                .containsExactlyInAnyOrderElementsOf(activeInstanceIds);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when suspended instances async if engine is not available")
    void givenActiveInstancesAndNotAvailableEngine_whenSuspendByIdsAsync_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 5);

        String processId = sampleDataManager.getDeployedProcessVersions("visitPlanning").get(0);
        List<String> activeInstanceIds = camundaRestTestHelper.getActiveInstancesByProcessId(camunda7, processId);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> processInstanceService.suspendByIdsAsync(activeInstanceIds))
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
