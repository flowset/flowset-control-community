/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition;

import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ProcessDefinitionFindAllTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ApplicationContext applicationContext;


    @Test
    @DisplayName("Empty list returned if engine is not selected")
    void givenDeployedProcessesAndNoSelectedEngine_whenFindAll_thenEmptyListReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        resetSelectedEngine();

        ProcessDefinitionLoadContext loadContext = new ProcessDefinitionLoadContext();

        //when
        List<ProcessDefinitionData> foundProcesses = processDefinitionService.findAll(loadContext);

        //then
        assertThat(foundProcesses).isEmpty();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get process definitions list if engine is not available")
    void givenDeployedProcessesAndNotAvailableEngine_whenFindAll_thenExceptionThrown() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/vacationApproval.bpmn");

        camunda7.stop();

        ProcessDefinitionLoadContext loadContext = new ProcessDefinitionLoadContext();

        //when and then
        assertThatThrownBy(() -> processDefinitionService.findAll(loadContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("All process definitions returned if load context is empty")
    void givenDeployedProcessesAndEmptyContext_whenFindAll_thenAllProcessVersionsReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .deploy("test_support/vacationApproval.bpmn");


        ProcessDefinitionLoadContext loadContext = new ProcessDefinitionLoadContext();

        //when
        List<ProcessDefinitionData> foundProcesses = processDefinitionService.findAll(loadContext);

        //then
        assertThat(foundProcesses)
                .hasSize(3)
                .extracting(ProcessDefinitionData::getKey, ProcessDefinitionData::getVersion, ProcessDefinitionData::getVersionTag, ProcessDefinitionData::getResourceName)
                .containsExactlyInAnyOrder(
                        tuple("visitPlanning", 1, "v1", "testVisitPlanningV1.bpmn"),
                        tuple("visitPlanning", 2, "v2", "testVisitPlanningV2.bpmn"),
                        tuple("vacation_approval", 1, null, "vacationApproval.bpmn")
                );
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Page with process definitions returned if load context contains valid page number and size")
    void givenDeployedProcessesAndContextWithValidPagination_whenFindAll_thenAllProcessVersionsReturned(int firstResult, int maxResults, int expectedCount) {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .deploy("test_support/vacationApproval.bpmn");

        ProcessDefinitionLoadContext loadContext = new ProcessDefinitionLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<ProcessDefinitionData> foundProcesses = processDefinitionService.findAll(loadContext);

        //then
        assertThat(foundProcesses)
                .hasSize(expectedCount);
    }




    private static Stream<Arguments> provideValidPaginationData() {
        return Stream.of(
                Arguments.of(0, 2, 2),
                Arguments.of(2, 4, 1)
        );
    }
}
