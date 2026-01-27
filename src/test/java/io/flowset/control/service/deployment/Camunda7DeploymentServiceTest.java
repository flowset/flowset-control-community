/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment;

import io.flowset.control.entity.filter.DeploymentFilter;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.jmix.core.DataManager;
import io.jmix.core.Resources;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.exception.RemoteEngineParseException;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.dto.response.DeploymentDto;
import io.flowset.control.test_support.camunda7.dto.response.DeploymentResultDto;
import io.flowset.control.test_support.camunda7.dto.response.ProcessDefinitionDto;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7DeploymentServiceTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    DataManager dataManager;


    @Autowired
    DeploymentService deploymentService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Autowired
    Resources resources;

    @Test
    @DisplayName("Deploy valid BPMN 2.0 XML with filename as resource name")
    void givenResourceNameAndValidBpmnXml_whenDeployWithContext_thenProcessDeployed() {
        //given
        String resourceName = "contractApproval.bpmn";
        String bpmnXml = getResource("test_support/contractApproval.bpmn");
        DeploymentContext deploymentContext = new DeploymentContext(resourceName, new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));

        //when
        DeploymentWithDefinitions deployment = deploymentService.createDeployment(deploymentContext);

        //then
        assertThat(deployment).isNotNull();
        assertThat(deployment.getDeployedProcessDefinitions()).hasSize(1);

        DeploymentDto foundDeployment = camundaRestTestHelper.findDeployment(camunda7, deployment.getId());
        assertThat(foundDeployment).isNotNull();
        assertThat(foundDeployment.getName()).isNull();
        assertThat(foundDeployment.getSource()).isEqualTo("Flowset Control");
        assertThat(foundDeployment.getId()).isEqualTo(deployment.getId());

        List<ProcessDefinitionDto> processVersions = camundaRestTestHelper.getProcessesByDeploymentId(camunda7, deployment.getId());
        assertThat(processVersions)
                .hasSize(1)
                .first()
                .satisfies(processDefinitionDto -> {
                    assertThat(processDefinitionDto.getKey()).isEqualTo("contractApproval");
                    assertThat(processDefinitionDto.getName()).isEqualTo("Contract approval");
                    assertThat(processDefinitionDto.getResource()).isEqualTo("contractApproval.bpmn");

                    String savedXml = camundaRestTestHelper.getBpmnXml(camunda7, processDefinitionDto.getId());
                    assertThat(savedXml).isNotNull();
                    assertThat(savedXml).isEqualTo(bpmnXml);
                });
    }

    @Test
    @DisplayName("RemoteEngineParseException occurs if deploy invalid BPMN 2.0 XML")
    void givenResourceNameAndInvalidBpmnXml_whenDeployWithContext_thenExceptionThrown() {
        //given
        String resourceName = "testDeployInvalidBpmnXml.bpmn";
        String bpmnXml = getResource("test_support/testDeployInvalidBpmnXml.bpmn");
        DeploymentContext deploymentContext = new DeploymentContext(resourceName, new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));

        //when and then
        assertThatThrownBy(() -> deploymentService.createDeployment(deploymentContext))
                .isInstanceOf(RemoteEngineParseException.class)
                .satisfies(exception -> {
                    RemoteEngineParseException parseException = (RemoteEngineParseException) exception;
                    assertThat(parseException.getDetails())
                            .hasSize(1)
                            .containsKey(resourceName)
                            .extractingByKey(resourceName)
                            .satisfies(resourceReport -> {
                                assertThat(resourceReport.getErrors()).hasSize(1);
                            });
                });

    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when deploy a valid BPMN 2.0 if engine is not available")
    void givenResourceNameAndValidaBpmnXmlNotAvailableEngine_whenDeployWithContent_thenExceptionThrown() {
        //given
        String resourceName = "contractApproval.bpmn";
        String bpmnXml = getResource("test_support/contractApproval.bpmn");
        DeploymentContext deploymentContext = new DeploymentContext(resourceName, new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> deploymentService.createDeployment(deploymentContext))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Find deployment by existing id")
    void givenExistingDeployment_whenFindById_thenDeploymentReturned() {
        //given
        DeploymentResultDto deployment = camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");

        //when
        DeploymentData foundDeployment = deploymentService.findById(deployment.getId());

        //then
        assertThat(foundDeployment).isNotNull();
        assertThat(foundDeployment.getDeploymentId()).isEqualTo(deployment.getId());
        assertThat(foundDeployment.getName()).isEqualTo("supportRequest.bpmn");
        assertThat(foundDeployment.getSource()).isNull();
        assertThat(foundDeployment.getDeploymentTime()).isNotNull();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when find existing deployment by id if engine is not available")
    void givenExistingDeploymentNotAvailableEngine_whenFindById_thenExceptionThrown() {
        //given
        DeploymentResultDto deployment = camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> deploymentService.findById(deployment.getId()))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("Get count of all deployments if filter is null")
    void givenDeployedResourcesAndNullFilter_whenGetCount_thenAllDeploymentsCountReturned() {
        //given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");
        camundaRestTestHelper.createDeployment(camunda7, "test_support/contractApproval.bpmn");

        //when
        long count = deploymentService.getCount(null);

        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Get count of deployments filtered by name like")
    void givenDeployedResourcesAndFilterByName_whenGetCount_thenAllDeploymentsCountReturned() {
        //given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");
        camundaRestTestHelper.createDeployment(camunda7, "test_support/vacationApproval.bpmn");

        DeploymentFilter filter = dataManager.create(DeploymentFilter.class);
        filter.setNameLike("Approval");

        //when
        long count = deploymentService.getCount(filter);

        //then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Get count of deployments filtered by name like")
    void givenDeployedResourcesAndFilterByKey_whenGetCount_thenAllDeploymentsCountReturned() {
        //given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");
        camundaRestTestHelper.createDeployment(camunda7, "test_support/contractApproval.bpmn");

        DeploymentFilter filter = dataManager.create(DeploymentFilter.class);
        filter.setNameLike("Approval");

        //when
        long count = deploymentService.getCount(filter);

        //then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Get count of deployments filtered by deployment after")
    void givenDeployedResourcesAndDeploymentAfter_whenGetCount_thenAllDeploymentsCountReturned() {
        //given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");
        camundaRestTestHelper.createDeployment(camunda7, "test_support/contractApproval.bpmn");

        DeploymentFilter filter = dataManager.create(DeploymentFilter.class);
        filter.setDeploymentAfter(OffsetDateTime.now().plusHours(1));

        //when
        long count = deploymentService.getCount(filter);

        //then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("Get count of deployments filtered by deployment before")
    void givenDeployedResourcesAndDeploymentBefore_whenGetCount_thenAllDeploymentsCountReturned() {
        //given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");
        camundaRestTestHelper.createDeployment(camunda7, "test_support/contractApproval.bpmn");

        DeploymentFilter filter = dataManager.create(DeploymentFilter.class);
        filter.setDeploymentBefore(OffsetDateTime.now().minusHours(1));

        //when
        long count = deploymentService.getCount(filter);

        //then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when get deployments count if engine is not available")
    void givenDeployedResourcesAndNotAvailableEngine_whenGetCount_thenExceptionThrown() {
        //given
        camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");
        camundaRestTestHelper.createDeployment(camunda7, "test_support/contractApproval.bpmn");

        camunda7.stop();


        //when and then
        assertThatThrownBy(() -> deploymentService.getCount(null))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    private String getResource(String name) {
        String resourceAsString = resources.getResourceAsString(name);
        if (resourceAsString == null) {
            throw new IllegalStateException("Resource " + name + " not found");
        }
        return resourceAsString;
    }
}
