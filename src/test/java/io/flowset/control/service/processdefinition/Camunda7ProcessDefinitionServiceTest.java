/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition;

import io.flowset.control.exception.EngineConnectionFailedException;
import io.jmix.core.DataManager;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.DeploymentResultDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7ProcessDefinitionServiceTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private DataManager dataManager;

    @Test
    @DisplayName("Load only latest versions of the deployed processes")
    void givenDeployedProcessesWithMultipleVersions_whenFindLatestVersions_thenOnlyLatestVersionsReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        //when
        List<ProcessDefinitionData> latestVersions = processDefinitionService.findLatestVersions();

        //then
        assertThat(latestVersions).hasSize(1)
                .extracting(ProcessDefinitionData::getKey, ProcessDefinitionData::getName, ProcessDefinitionData::getVersionTag,
                        ProcessDefinitionData::getVersion)
                .contains(tuple("visitPlanning", "Visit planning", "v2", 2));
    }

    @Test
    @DisplayName("Empty list with latest versions are returned if engine is not selected")
    void givenDeployedProcessesAndNoSelectedEngine_whenFindLatestVersions_thenEmptyListReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        resetSelectedEngine();

        //when
        List<ProcessDefinitionData> latestVersions = processDefinitionService.findLatestVersions();

        //then
        assertThat(latestVersions).isEmpty();
    }

    @Test
    @DisplayName("Empty list with latest versions are returned if engine is not available")
    void givenDeployedProcessesAndNotAvailableEngine_whenFindLatestVersions_thenEmptyListReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        camunda7.stop();

        //when
        List<ProcessDefinitionData> latestVersions = processDefinitionService.findLatestVersions();

        //then
        assertThat(latestVersions).isEmpty();
    }

    @Test
    @DisplayName("Load all process versions by process key")
    void givenDeployedProcessesWithMultipleVersions_whenFindAllByKey_thenAllVersionsReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        //when
        List<ProcessDefinitionData> allVersions = processDefinitionService.findAllByKey("visitPlanning");

        //then
        assertThat(allVersions)
                .hasSize(2)
                .extracting(ProcessDefinitionData::getKey, ProcessDefinitionData::getVersionTag)
                .contains(tuple("visitPlanning", "v1"), tuple("visitPlanning", "v2"));
    }

    @Test
    @DisplayName("Empty list with versions are returned by process key if engine is not selected")
    void givenDeployedProcessesAndNoSelectedEngine_whenFindAllByKey_thenNoVersionsReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        resetSelectedEngine();

        //when
        List<ProcessDefinitionData> allVersions = processDefinitionService.findAllByKey("visitPlanning");

        //then
        assertThat(allVersions).isEmpty();
    }

    @Test
    @DisplayName("Empty list with versions are returned by process key if engine is not available")
    void givenDeployedProcessesAndNotAvailableEngine_whenFindAllByKey_thenNoVersionsReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        camunda7.stop();

        //when
        List<ProcessDefinitionData> allVersions = processDefinitionService.findAllByKey("visitPlanning");

        //then
        assertThat(allVersions).isEmpty();
    }

    @Test
    @DisplayName("Process version found by id")
    void givenDeployedProcess_whenGetById_thenProcessDataReturned() {
        //given
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "test_support/testVisitPlanningV1.bpmn");
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        //when
        ProcessDefinitionData foundProcess = processDefinitionService.getById(processId);

        //then
        assertThat(foundProcess).isNotNull();
        assertThat(foundProcess.getProcessDefinitionId()).isEqualTo(processId);
        assertThat(foundProcess.getKey()).isEqualTo("visitPlanning");
        assertThat(foundProcess.getVersionTag()).isEqualTo("v1");
        assertThat(foundProcess.getVersion()).isEqualTo(1);
        assertThat(foundProcess.getHistoryTimeToLive()).isEqualTo(10);
        assertThat(foundProcess.getName()).isEqualTo("Visit planning");
        assertThat(foundProcess.getDescription()).isEqualTo("Simple process to schedule a visit by date");
        assertThat(foundProcess.getDeploymentId()).isNotNull();
        assertThat(foundProcess.getResourceName()).isEqualTo("testVisitPlanningV1.bpmn");
        assertThat(foundProcess.getStartableInTaskList()).isTrue();
    }

    @Test
    @DisplayName("Process version not found by non-existing id")
    void givenNonExistingProcessId_whenLoadProcessesById_thenNullReturned() {
        //given
        String processId = UUID.randomUUID().toString();

        //when
        ProcessDefinitionData foundProcess = processDefinitionService.getById(processId);

        //then
        assertThat(foundProcess).isNull();
    }

    @Test
    @DisplayName("Process version not found if no engine selected")
    void givenDeployedProcessIdAndNoSelectedEngine_whenGetById_thenNullReturned() {
        //given
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "test_support/testVisitPlanningV1.bpmn");
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        resetSelectedEngine();

        //when
        ProcessDefinitionData foundProcess = processDefinitionService.getById(processId);

        //then
        assertThat(foundProcess).isNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException when find process version by id if engine is not available")
    void givenDeployedProcessIdAndStoppedEngine_whenGetById_thenExceptionThrown() {
        //given
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "test_support/testVisitPlanningV1.bpmn");
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> processDefinitionService.getById(processId))
                .isInstanceOf(EngineConnectionFailedException.class);
    }


    @Test
    @DisplayName("Get BPMN XML of deployed process by process id")
    void givenBpmnXmlAndDeployedProcess_whenGetBpmnXmlByProcessId_thenXmlStringReturned() {
        //given
        String sourceBpmnXml = """
                 <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" xmlns:modeler="http://camunda.org/schema/modeler/1.0" id="Definitions_1at2m4z" targetNamespace="http://bpmn.io/schema/bpmn" exporter="Camunda Modeler" exporterVersion="5.23.0" modeler:executionPlatform="Camunda Platform" modeler:executionPlatformVersion="7.21.0">
                   <bpmn:process id="emptyProcess" name="Empty process" isExecutable="true" camunda:historyTimeToLive="10">
                     <bpmn:startEvent id="StartEvent_1" />
                   </bpmn:process>
                   <bpmndi:BPMNDiagram id="BPMNDiagram_1">
                     <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="emptyProcess">
                       <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
                         <dc:Bounds x="179" y="79" width="36" height="36" />
                       </bpmndi:BPMNShape>
                     </bpmndi:BPMNPlane>
                   </bpmndi:BPMNDiagram>
                 </bpmn:definitions>
                """;
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "emptyProcess.bpmn", sourceBpmnXml);
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        //when
        String foundBpmnXml = processDefinitionService.getBpmnXml(processId);

        //then
        assertThat(foundBpmnXml).isNotNull();
        assertThat(foundBpmnXml).isEqualTo(sourceBpmnXml);
    }

    @Test
    @DisplayName("BPMN XML of deployed process not found by non-existing id")
    void givenNonExistingProcessId_whenGetBpmnXmlByProcessId_thenNullStringReturned() {
        //given
        String processId = UUID.randomUUID().toString();

        //when
        String bpmnXml = processDefinitionService.getBpmnXml(processId);

        //then
        assertThat(bpmnXml).isNull();
    }

    @Test
    @DisplayName("BPMN XML not found if engine is not available")
    void givenBpmnXmlAndDeployedProcessAndStoppedEngine_whenGetBpmnXml_thenNullStringReturned() {
        //given
        String sourceBpmnXml = """
                 <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" xmlns:modeler="http://camunda.org/schema/modeler/1.0" id="Definitions_1at2m4z" targetNamespace="http://bpmn.io/schema/bpmn" exporter="Camunda Modeler" exporterVersion="5.23.0" modeler:executionPlatform="Camunda Platform" modeler:executionPlatformVersion="7.21.0">
                   <bpmn:process id="emptyProcess" name="Empty process" isExecutable="true" camunda:historyTimeToLive="10">
                     <bpmn:startEvent id="StartEvent_1" />
                   </bpmn:process>
                   <bpmndi:BPMNDiagram id="BPMNDiagram_1">
                     <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="emptyProcess">
                       <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
                         <dc:Bounds x="179" y="79" width="36" height="36" />
                       </bpmndi:BPMNShape>
                     </bpmndi:BPMNPlane>
                   </bpmndi:BPMNDiagram>
                 </bpmn:definitions>
                """;
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "emptyProcess.bpmn", sourceBpmnXml);
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        camunda7.stop();

        //when
        String foundBpmnXml = processDefinitionService.getBpmnXml(processId);

        //then
        assertThat(foundBpmnXml).isNull();
    }

    @Test
    @DisplayName("Null BPMN XML is returned by process id if engine is not selected")
    void givenDeployedProcessAndNoSelectedEngine_whenGetBpmnXml_thenNullReturned() {
        //given
        DeploymentResultDto deploymentResultDto = camundaRestTestHelper.createDeployment(camunda7, "test_support/testVisitPlanningV1.bpmn");
        String processId = deploymentResultDto.getDeployedProcessDefinitions().keySet().iterator().next();

        resetSelectedEngine();

        //when
        String bpmnXml = processDefinitionService.getBpmnXml(processId);

        //then
        assertThat(bpmnXml).isNull();
    }

    @Test
    @DisplayName("Zero is returned as process versions count if engine is not selected")
    void givenDeployedProcessAndNoSelectedEngine_whenGetCount_thenZeroReturned() {
        //given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/testVisitPlanningV1.bpmn");

        resetSelectedEngine();

        //when
        long processCount = processDefinitionService.getCount(null);

        //then
        assertThat(processCount).isZero();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get process versions count if engine is not available")
    void givenDeployedProcessAndNotAvailableEngine_whenGetCount_thenExceptionThrown() {
        //given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/testVisitPlanningV1.bpmn");

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> processDefinitionService.getCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Count of all process versions is returned if filter is null")
    void givenDeployedProcessAndNullFilter_whenGetCount_thenAllProcessCountReturned() {
        //given
        applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .deploy("test_support/testVisitPlanningV2.bpmn")
                .deploy("test_support/vacationApproval.bpmn");

        //when
        long processCount = processDefinitionService.getCount(null);

        //then
        assertThat(processCount).isEqualTo(3);
    }
}
