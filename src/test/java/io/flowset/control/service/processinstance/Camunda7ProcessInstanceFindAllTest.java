/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ProcessInstanceFindAllTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    ApplicationContext applicationContext;


    @Test
    @DisplayName("Load all historic process instances with empty load context")
    void givenRunningProcessInstancesAndEmptyContext_whenFindAllHistoricInstances_thenAllInstancesReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 2);

        ProcessInstanceLoadContext loadContext = new ProcessInstanceLoadContext();

        //when
        List<ProcessInstanceData> instances = processInstanceService.findAllHistoricInstances(loadContext);

        //then
        assertThat(instances).isNotNull()
                .hasSize(2);
    }

    @Test
    @DisplayName("Empty list returned if no selected engine")
    void givenRunningProcessInstancesAndNoSelectedEngine_whenFindAllHistoricInstances_thenEmptyListReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        resetSelectedEngine();

        ProcessInstanceLoadContext loadContext = new ProcessInstanceLoadContext();

        //when
        List<ProcessInstanceData> instances = processInstanceService.findAllHistoricInstances(loadContext);

        //then
        assertThat(instances).isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get instance list if selected engine is not available")
    void givenRunningProcessInstancesAndNotAvailableEngine_whenFindAllHistoricInstances_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval");

        camunda7.stop();

        ProcessInstanceLoadContext loadContext = new ProcessInstanceLoadContext();

        //when adn then
        assertThatThrownBy(() -> processInstanceService.findAllHistoricInstances(loadContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Load a page with historic process instances")
    void givenRunningProcessInstanceAndContext_whenFindAllHistoricInstances_thenStartedInstanceDataReturned(int firstResult, int maxResults,
                                                                                                            int expectedCount) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/vacationApproval.bpmn")
                .startByKey("vacation_approval", 3);

        ProcessInstanceLoadContext loadContext = new ProcessInstanceLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<ProcessInstanceData> instances = processInstanceService.findAllHistoricInstances(loadContext);

        //then
        assertThat(instances).hasSize(expectedCount);
    }


    static Stream<Arguments> provideValidPaginationData() {
        return Stream.of(
                Arguments.of(0, 2, 2),
                Arguments.of(2, 4, 1)
        );
    }
}
