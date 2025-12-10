/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import io.jmix.core.DataManager;
import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.RemoteProcessEngineException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.dto.response.DeploymentResultDto;
import io.flowset.control.test_support.camunda7.dto.response.ProcessVariablesMapDto;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeProcessInstanceDto;
import io.flowset.control.test_support.camunda7.dto.response.VariableInstanceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ProcessInstanceStartTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    DataManager dataManager;

    @Test
    @DisplayName("ConnectionException is thrown on process instance start if selected engine is not available")
    void givenDeployedProcessAndNotAvailableEngine_whenStartByProcessId_thenStartedInstanceDataReturned() {
        //given
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        camunda7.stop();

        Collection<VariableInstanceData> variables = new ArrayList<>();

        //when and then
        assertThatThrownBy(() -> processInstanceService.startProcessByDefinitionId(processId, variables, null))
                .hasCauseInstanceOf(ConnectException.class);
    }

    @Test
    @DisplayName("Process instance created if start process by id with empty variables map")
    void givenDeployedProcessAndEmptyVariables_whenStartByProcessId_thenStartedInstanceDataReturned() {
        //given
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        Collection<VariableInstanceData> variables = new ArrayList<>();

        //when
        ProcessInstanceData startedInstance = processInstanceService.startProcessByDefinitionId(processId, variables, null);

        //then
        assertThat(startedInstance).isNotNull();
        assertThat(startedInstance.getInstanceId()).isNotNull();
        assertThat(startedInstance.getBusinessKey()).isNull();

        RuntimeProcessInstanceDto foundInstance = camundaRestTestHelper.findRuntimeInstance(camunda7, startedInstance.getInstanceId());

        assertThat(foundInstance).isNotNull();
        assertThat(foundInstance.getDefinitionId()).isEqualTo(processId);
        assertThat(foundInstance.getBusinessKey()).isNull();
        assertThat(foundInstance.getCaseInstanceId()).isNull();
    }

    @Test
    @DisplayName("Process instance with business key created")
    void givenDeployedProcessAndBusinessKey_whenStartByProcessId_thenStartedInstanceWithBkDataReturned() {
        //given
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();
        String businessKey = "Employee: test";

        Collection<VariableInstanceData> variables = new ArrayList<>();

        //when
        ProcessInstanceData startedInstance = processInstanceService.startProcessByDefinitionId(processId, variables, businessKey);

        //then
        assertThat(startedInstance).isNotNull();
        assertThat(startedInstance.getInstanceId()).isNotNull();
        assertThat(startedInstance.getBusinessKey()).isEqualTo("Employee: test");

        RuntimeProcessInstanceDto foundInstance = camundaRestTestHelper.findRuntimeInstance(camunda7, startedInstance.getInstanceId());

        assertThat(foundInstance).isNotNull();
        assertThat(foundInstance.getDefinitionId()).isEqualTo(processId);
        assertThat(foundInstance.getBusinessKey()).isEqualTo("Employee: test");
        assertThat(foundInstance.getCaseInstanceId()).isNull();
    }

    @Test
    @DisplayName("RemoteProcessEngineException thrown if start process by non-existing process id")
    void givenNonExistingProcessId_whenStartByProcessId_thenRemoteEngineExceptionThrown() {
        //given
        String processId = UUID.randomUUID().toString();

        Collection<VariableInstanceData> variables = new ArrayList<>();

        //when and then
        assertThatThrownBy(() -> processInstanceService.startProcessByDefinitionId(processId, variables, null))
                .isInstanceOf(RemoteProcessEngineException.class)
                .hasMessageContaining("no deployed process definition found with id '%s'".formatted(processId));

        long runningProcessesCount = camundaRestTestHelper.getRunningProcessesCount(camunda7);
        assertThat(runningProcessesCount).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("provideNonNullPrimitiveVariables")
    @DisplayName("Process instance created if start process by id with non-null primitive variable")
    void givenDeployedProcessAndNonNullPrimitiveVariable_whenStartByProcessId_thenInstanceStartedWithVariable(String variableType, Object variableValue) {
        //given
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        VariableInstanceData variableInstance = createVariableInstance("myVar", variableType, variableValue);
        List<VariableInstanceData> variables = List.of(variableInstance);

        //when
        ProcessInstanceData startedInstance = processInstanceService.startProcessByDefinitionId(processId, variables, null);

        //then: check instance and variables in engine
        assertThat(startedInstance).isNotNull();
        assertThat(startedInstance.getInstanceId()).isNotNull();

        RuntimeProcessInstanceDto instanceFromCamunda = camundaRestTestHelper.findRuntimeInstance(camunda7, startedInstance.getInstanceId());

        assertThat(instanceFromCamunda).isNotNull();
        assertThat(instanceFromCamunda.getDefinitionId()).isEqualTo(processId);
        assertThat(instanceFromCamunda.getBusinessKey()).isNull();
        assertThat(instanceFromCamunda.getCaseInstanceId()).isNull();

        ProcessVariablesMapDto foundVariables = camundaRestTestHelper.getVariablesByProcess(camunda7, instanceFromCamunda.getId());
        assertThat(foundVariables)
                .isNotNull()
                .hasSize(1)
                .extractingByKey("myVar")
                .isNotNull()
                .extracting(VariableInstanceDto::getType, VariableInstanceDto::getValue)
                .contains(variableType, variableValue);
    }

    private static Stream<Arguments> provideNonNullPrimitiveVariables() {
        return Stream.of(
                Arguments.of("String", "My string var"),
                Arguments.of("Integer", Integer.MAX_VALUE),
                Arguments.of("Long", Long.MAX_VALUE),
                Arguments.of("Boolean", true),
                Arguments.of("Double", 15.5d)
        );
    }

    private VariableInstanceData createVariableInstance(String name, String type, @Nullable Object value) {
        VariableInstanceData variableInstanceData = dataManager.create(VariableInstanceData.class);
        variableInstanceData.setName(name);
        variableInstanceData.setType(type);
        variableInstanceData.setValue(value);

        return variableInstanceData;
    }
}
