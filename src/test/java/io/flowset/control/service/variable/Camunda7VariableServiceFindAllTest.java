/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable;

import io.jmix.core.DataManager;
import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.variable.VariableInstanceData;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7VariableServiceFindAllTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    VariableService variableService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("Return all runtime variables if load context is empty")
    void givenExistingVariablesAndEmptyContext_whenFindRuntimeVariables_thenAllVariablesReturned() {
        //given

       applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build())
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("secondVariable", new VariableValueDto("String", "Some another value"))
                        .build());

       VariableLoadContext loadContext = new VariableLoadContext();

        //when
       List<VariableInstanceData> runtimeVariables = variableService.findRuntimeVariables(loadContext);

       //then
       assertThat(runtimeVariables).hasSize(2)
               .extracting(VariableInstanceData::getName, VariableInstanceData::getValue, VariableInstanceData::getType)
               .containsExactlyInAnyOrder(
                       tuple("firstVariable", "Some value", "String"),
                       tuple("secondVariable", "Some another value", "String")
               );
    }

    @ParameterizedTest
    @MethodSource("provideValidPaginationData")
    @DisplayName("Load a page with runtime variables")
    void givenContextWithPagination_whenFindRuntimeVariables_thenPageWithVariablesReturned(int firstResult, int maxResults,
                                                                                           int expectedCount) {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testUpdateVariable.bpmn")
                .startByKey("testUpdateVariable", StartProcessDto.builder()
                        .variable("firstVariable", new VariableValueDto("String", "Some value"))
                        .build(), 3);

        VariableLoadContext loadContext = new VariableLoadContext()
                .setFirstResult(firstResult)
                .setMaxResults(maxResults);

        //when
        List<VariableInstanceData> runtimeVariables = variableService.findRuntimeVariables(loadContext);

        //then
        assertThat(runtimeVariables).hasSize(expectedCount);
    }

    @Test
    @DisplayName("Local flag is set for variable of a user task running in a sub-process")
    void givenTaskLocalVariable_whenFindRuntimeVariables_thenLocalFlagSet() {
        //given
        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess");

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);
        String taskId = camundaRestTestHelper.getUserTasksByInstanceIds(camunda7, processInstanceId).getFirst();

        camundaRestTestHelper.putLocalTaskVariable(camunda7, taskId, "taskVariable",
                new VariableValueDto("String", "taskValue"));

        VariableFilter filter = dataManager.create(VariableFilter.class);
        filter.setProcessInstanceId(processInstanceId);

        //when
        List<VariableInstanceData> variables = variableService.findRuntimeVariables(
                new VariableLoadContext().setFilter(filter));

        //then
        assertThat(variables)
                .extracting(VariableInstanceData::getName, VariableInstanceData::getExecutionId,
                        VariableInstanceData::getLocal)
                .containsExactly(tuple("taskVariable", subProcessExecutionId, true));
    }

    @Test
    @DisplayName("Return only variables of the sub-process execution")
    void givenLocalAndGlobalVariables_whenFindRuntimeVariablesByExecutionId_thenOnlyExecutionScopeVariablesReturned() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "localVariable",
                new VariableValueDto("String", "localValue"));

        VariableFilter filter = dataManager.create(VariableFilter.class);
        filter.setExecutionId(subProcessExecutionId);

        //when
        List<VariableInstanceData> variables = variableService.findRuntimeVariables(
                new VariableLoadContext().setFilter(filter));

        //then
        assertThat(variables)
                .extracting(VariableInstanceData::getName)
                .containsExactly("localVariable");
    }

    @Test
    @DisplayName("Variables of a sub-process execution are not returned for the process instance execution")
    void givenProcessInstanceExecutionId_whenFindRuntimeVariablesByExecutionId_thenOnlyProcessScopeVariablesReturned() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "localVariable",
                new VariableValueDto("String", "localValue"));

        VariableFilter filter = dataManager.create(VariableFilter.class);
        filter.setExecutionId(processInstanceId);

        //when
        List<VariableInstanceData> variables = variableService.findRuntimeVariables(
                new VariableLoadContext().setFilter(filter));

        //then
        assertThat(variables)
                .extracting(VariableInstanceData::getName)
                .containsExactly("globalVariable");
    }

    @Test
    @DisplayName("The same variable name is counted once in each scope")
    void givenSameNameInTwoScopes_whenGetRuntimeVariablesCount_thenCountIsOnePerScope() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("sharedName", new VariableValueDto("String", "globalValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        camundaRestTestHelper.putLocalExecutionVariable(camunda7, subProcessExecutionId, "sharedName",
                new VariableValueDto("String", "localValue"));

        VariableFilter processScopeFilter = dataManager.create(VariableFilter.class);
        processScopeFilter.setExecutionId(processInstanceId);
        processScopeFilter.setVariableName("sharedName");

        VariableFilter localScopeFilter = dataManager.create(VariableFilter.class);
        localScopeFilter.setExecutionId(subProcessExecutionId);
        localScopeFilter.setVariableName("sharedName");

        //when
        long processScopeCount = variableService.getRuntimeVariablesCount(processScopeFilter);
        long localScopeCount = variableService.getRuntimeVariablesCount(localScopeFilter);

        //then
        assertThat(processScopeCount).isEqualTo(1);
        assertThat(localScopeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Zero returned when the scope has no variable with the specified name")
    void givenExecutionIdAndVariableNameFilters_whenGetRuntimeVariablesCount_thenZeroReturned() {
        //given
        StartProcessDto startProcessDto = StartProcessDto.builder()
                .variable("globalVariable", new VariableValueDto("String", "globalValue"))
                .build();

        CamundaSampleDataManager camundaSampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testLocalVariableSubProcess.bpmn")
                .startByKey("testLocalVariableSubProcess", startProcessDto);

        String processInstanceId = camundaSampleDataManager.getStartedInstances("testLocalVariableSubProcess").get(0);
        String subProcessExecutionId = findSubProcessExecutionId(processInstanceId);

        VariableFilter filter = dataManager.create(VariableFilter.class);
        filter.setExecutionId(subProcessExecutionId);
        filter.setVariableName("globalVariable");

        //when
        long variablesCount = variableService.getRuntimeVariablesCount(filter);

        //then
        assertThat(variablesCount).isZero();
    }

    String findSubProcessExecutionId(String processInstanceId) {
        return camundaRestTestHelper.findExecutions(camunda7, processInstanceId)
                .stream()
                .map(ExecutionDto::getId)
                .filter(executionId -> !executionId.equals(processInstanceId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No child execution found for " + processInstanceId));
    }

    static Stream<Arguments> provideValidPaginationData() {
        return Stream.of(
                Arguments.of(0, 2, 2),
                Arguments.of(2, 4, 1)
        );
    }
}
