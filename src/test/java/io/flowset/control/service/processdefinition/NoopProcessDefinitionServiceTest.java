/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition;

import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
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
public class NoopProcessDefinitionServiceTest extends AbstractIntegrationTest {

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Test
    @DisplayName("Empty list of latest process versions returned when no engine exists")
    void givenNoEngine_whenFindLatestVersions_thenEmptyListReturned() {
        //when
        List<ProcessDefinitionData> processDefinitions = processDefinitionService.findLatestVersions();

        //then
        assertThat(processDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Empty list of process definitions returned when no engine exists")
    void givenNoEngine_whenFindAll_thenEmptyListReturned() {
        //when
        List<ProcessDefinitionData> processDefinitions = processDefinitionService
                .findAll(new ProcessDefinitionLoadContext());

        //then
        assertThat(processDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Empty list of process versions by key returned when no engine exists")
    void givenNoEngine_whenFindAllByKey_thenEmptyListReturned() {
        //when
        List<ProcessDefinitionData> processDefinitions = processDefinitionService
                .findAllByKey("processDefinitionKey");

        //then
        assertThat(processDefinitions).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as process definitions count when no engine exists")
    void givenNoEngine_whenGetCount_thenZeroReturned() {
        //when
        long count = processDefinitionService.getCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for process definition when no engine exists")
    void givenNoEngine_whenGetById_thenNullReturned() {
        //when
        ProcessDefinitionData processDefinition = processDefinitionService.getById("processDefinitionId");

        //then
        assertThat(processDefinition).isNull();
    }

    @Test
    @DisplayName("Null returned as BPMN XML when no engine exists")
    void givenNoEngine_whenGetBpmnXml_thenNullReturned() {
        //when
        String bpmnXml = processDefinitionService.getBpmnXml("processDefinitionId");

        //then
        assertThat(bpmnXml).isNull();
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when activate process definition if no engine exists")
    void givenNoEngine_whenActivateById_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processDefinitionService.activateById("processDefinitionId", true))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when activate all process versions if no engine exists")
    void givenNoEngine_whenActivateAllVersionsByKey_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processDefinitionService.activateAllVersionsByKey("processDefinitionKey", true))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when suspend process definition if no engine exists")
    void givenNoEngine_whenSuspendById_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processDefinitionService.suspendById("processDefinitionId", true))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when suspend all process versions if no engine exists")
    void givenNoEngine_whenSuspendAllVersionsByKey_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processDefinitionService.suspendAllVersionsByKey("processDefinitionKey", true))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when delete all process versions if no engine exists")
    void givenNoEngine_whenDeleteAllVersionsByKey_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processDefinitionService.deleteAllVersionsByKey("processDefinitionKey", true))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when delete process definition if no engine exists")
    void givenNoEngine_whenDeleteById_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> processDefinitionService.deleteById("processDefinitionId", true))
                .isInstanceOf(EngineNotSelectedException.class);
    }
}
