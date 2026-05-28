/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment;

import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.dto.response.DeploymentResultDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.flowset.control.test_support.security.role.deployment.TestDeploymentDeletePermissionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRunningEngine
public class Camunda7DeploymentServiceSecurityTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Delete deployment by id when user has delete permission")
    @WithTestUser(username = "test-user-secured-deployment-delete", roles = TestDeploymentDeletePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-deployment-delete")
    void givenUserWithDeploymentDeletePermission_whenDeleteById_thenDeploymentDeleted() {
        //given
        DeploymentResultDto deployment = camundaRestTestHelper.createDeployment(camunda7, "test_support/supportRequest.bpmn");

        //when
        deploymentService.deleteById(deployment.getId(), false, false, false);

        //then
        assertThat(camundaRestTestHelper.findDeployment(camunda7, deployment.getId())).isNull();
        assertThat(camundaRestTestHelper.getProcessesByDeploymentId(camunda7, deployment.getId())).isEmpty();
    }
}
