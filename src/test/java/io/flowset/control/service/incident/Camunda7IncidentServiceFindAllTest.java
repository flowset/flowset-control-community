/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.incident;

import io.flowset.control.entity.incident.HistoricIncidentData;
import io.flowset.control.entity.incident.IncidentData;
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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7IncidentServiceFindAllTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    IncidentService incidentService;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("Empty list with runtime incidents is returned if no selected engine")
    void givenEmptyLoadContextAndNoSelectedEngine_whenFindRuntimeIncidents_thenEmptyListReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 2)
                .waitJobsExecution();

        resetSelectedEngine();

        IncidentLoadContext loadContext = new IncidentLoadContext();

        //when
        List<IncidentData> runtimeIncidents = incidentService.findRuntimeIncidents(loadContext);

        //then
        assertThat(runtimeIncidents).isEmpty();
    }

    @Test
    @DisplayName("EngineConnectionFailedException is thrown when loading runtime incidents if selected engine is not available")
    void givenEmptyLoadContextAndNotAvailableEngine_whenFindRuntimeIncidents_thenExceptionThrown() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 2)
                .waitJobsExecution();

        camunda7.stop();

        IncidentLoadContext loadContext = new IncidentLoadContext();

        //when and then
        assertThatThrownBy(() -> incidentService.findRuntimeIncidents(loadContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Load all runtime incidents with empty load context")
    void givenEmptyLoadContext_whenFindRuntimeIncidents_thenAllIncidentsReturned() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 2)
                .waitJobsExecution();

        IncidentLoadContext loadContext = new IncidentLoadContext();

        //when
        List<IncidentData> runtimeIncidents = incidentService.findRuntimeIncidents(loadContext);

        //then
        assertThat(runtimeIncidents)
                .isNotNull()
                .hasSize(2)
                .extracting(IncidentData::getType, IncidentData::getActivityId)
                .contains(tuple("failedJob", "throwsExceptionTask"));
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Load a paginated list with runtime incidents")
    void givenLoadContextWithPageNumberAndSize_whenFindRuntimeIncidents_thenPageWithIncidentsReturned(int firstResult, int maxResults, int expectedCount) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 3)
                .waitJobsExecution();

        IncidentLoadContext loadContext = new IncidentLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<IncidentData> runtimeIncidents = incidentService.findRuntimeIncidents(loadContext);

        //then
        assertThat(runtimeIncidents)
                .isNotNull()
                .hasSize(expectedCount)
                .extracting(IncidentData::getType, IncidentData::getActivityId)
                .contains(tuple("failedJob", "throwsExceptionTask"));
    }

    @Test
    @DisplayName("Resolved incident is not returned in runtime incidents")
    void givenResolvedIncidentAndEmptyContext_whenFindRuntimeIncidents_thenOnlyRuntimeIncidentsReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs("testResolvedFailedJobIncident")
                .waitJobsExecution();

        String testFailedJobIncidentProcessId = sampleDataManager.getDeployedProcessVersions("testFailedJobIncident").get(0);

        IncidentLoadContext loadContext = new IncidentLoadContext();

        //when
        List<IncidentData> foundRuntimeIncidents = incidentService.findRuntimeIncidents(loadContext);

        //then
        assertThat(foundRuntimeIncidents).hasSize(1)
                .first()
                .satisfies(incidentData -> {
                    assertThat(incidentData.getProcessDefinitionId()).isEqualTo(testFailedJobIncidentProcessId);
                    assertThat(incidentData.getFailedActivityId()).isEqualTo("throwsExceptionTask");
                    assertThat(incidentData.getActivityId()).isEqualTo("throwsExceptionTask");
                });
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Load a paginated list with historic incidents")
    void givenLoadContextWithPageNumberAndSize_whenFindHistoricIncidents_thenPageWithIncidentsReturned(int firstResult, int maxResults, int expectedCount) {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        camundaSampleDataManager.deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident", 3)
                .waitJobsExecution();

        IncidentLoadContext loadContext = new IncidentLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<HistoricIncidentData> historicIncidents = incidentService.findHistoricIncidents(loadContext);

        //then
        assertThat(historicIncidents)
                .isNotNull()
                .hasSize(expectedCount)
                .extracting(HistoricIncidentData::getType, HistoricIncidentData::getActivityId)
                .contains(tuple("failedJob", "throwsExceptionTask"));
    }

    @Test
    @DisplayName("Resolved and open incidents are not returned in historic incidents")
    void givenOpenAndResolvedIncidentsAndEmptyContext_whenFindHistoricIncidents_thenResolvedAndOpenIncidentsReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7);
        sampleDataManager.deploy("test_support/testFailedJobIncident.bpmn")
                .startByKey("testFailedJobIncident")
                .deploy("test_support/testResolvedFailedJobIncident.bpmn")
                .startByKey("testResolvedFailedJobIncident")
                .waitJobsExecution()
                .retryFailedJobs("testResolvedFailedJobIncident")
                .waitJobsExecution();

        IncidentLoadContext loadContext = new IncidentLoadContext();

        //when
        List<HistoricIncidentData> foundRuntimeIncidents = incidentService.findHistoricIncidents(loadContext);

        //then
        assertThat(foundRuntimeIncidents).hasSize(2)
                .extracting(HistoricIncidentData::getProcessDefinitionKey, HistoricIncidentData::getActivityId, HistoricIncidentData::getResolved,
                        HistoricIncidentData::getOpen)
                .containsExactlyInAnyOrder(
                        tuple("testFailedJobIncident", "throwsExceptionTask", false, true),
                        tuple("testResolvedFailedJobIncident", "throwsExceptionConditionallyTask", true, false)
                );
    }

    static Stream<Arguments> provideValidPaginationData() {
        return Stream.of(
                Arguments.of(0, 2, 2),
                Arguments.of(2, 4, 1)
        );
    }
}
