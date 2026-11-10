/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.processinstance.RuntimeProcessInstanceData;
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
public class NoopProcessInstanceServiceTest extends AbstractIntegrationTest {

    @Autowired
    ProcessInstanceService processInstanceService;

    @Test
    @DisplayName("Empty list of historic instances returned when no engine exists")
    void givenNoEngine_whenFindAllHistoricInstances_thenEmptyListReturned() {
        //when
        List<ProcessInstanceData> instances = processInstanceService
                .findAllHistoricInstances(new ProcessInstanceLoadContext());

        //then
        assertThat(instances).isEmpty();
    }

    @Test
    @DisplayName("Empty list of runtime instances returned when no engine exists")
    void givenNoEngine_whenFindAllRuntimeInstances_thenEmptyListReturned() {
        //when
        List<RuntimeProcessInstanceData> instances = processInstanceService
                .findAllRuntimeInstances(new ProcessInstanceLoadContext());

        //then
        assertThat(instances).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as historic instances count when no engine exists")
    void givenNoEngine_whenGetHistoricInstancesCount_thenZeroReturned() {
        //when
        long count = processInstanceService.getHistoricInstancesCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as runtime instances count when no engine exists")
    void givenNoEngine_whenGetRuntimeInstancesCount_thenZeroReturned() {
        //when
        long count = processInstanceService.getRuntimeInstancesCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as instances count by process definition id when no engine exists")
    void givenNoEngine_whenGetCountByProcessDefinitionId_thenZeroReturned() {
        //when
        long count = processInstanceService.getCountByProcessDefinitionId("processDefinitionId");

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as instances count by deployment id when no engine exists")
    void givenNoEngine_whenGetCountByDeploymentId_thenZeroReturned() {
        //when
        long count = processInstanceService.getCountByDeploymentId("deploymentId");

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Zero returned as instances count by process definition key when no engine exists")
    void givenNoEngine_whenGetCountByProcessDefinitionKey_thenZeroReturned() {
        //when
        long count = processInstanceService.getCountByProcessDefinitionKey("processDefinitionKey");

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for process instance when no engine exists")
    void givenNoEngine_whenGetProcessInstanceById_thenNullReturned() {
        //when
        ProcessInstanceData instance = processInstanceService.getProcessInstanceById("instanceId");

        //then
        assertThat(instance).isNull();
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when start process if no engine exists")
    void givenNoEngine_whenStartProcessByDefinitionId_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processInstanceService.startProcessByDefinitionId("processDefinitionId", List.of(),
                null))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when suspend instance if no engine exists")
    void givenNoEngine_whenSuspendById_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processInstanceService.suspendById("instanceId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when activate instance if no engine exists")
    void givenNoEngine_whenActivateById_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processInstanceService.activateById("instanceId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when terminate instance if no engine exists")
    void givenNoEngine_whenTerminateById_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processInstanceService.terminateById("instanceId"))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when terminate instances async if no engine exists")
    void givenNoEngine_whenTerminateByIdsAsync_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processInstanceService.terminateByIdsAsync(
                new ProcessInstanceBulkTerminateContext(List.of("instanceId"))))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when activate instances async if no engine exists")
    void givenNoEngine_whenActivateByIdsAsync_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processInstanceService.activateByIdsAsync(List.of("instanceId")))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when suspend instances async if no engine exists")
    void givenNoEngine_whenSuspendByIdsAsync_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processInstanceService.suspendByIdsAsync(List.of("instanceId")))
                .isInstanceOf(EngineNotSelectedException.class);
    }
}
