/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment;

import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.entity.deployment.DeploymentResource;
import io.flowset.control.exception.EngineNotSelectedException;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class NoopDeploymentServiceTest extends AbstractIntegrationTest {

    @Autowired
    DeploymentService deploymentService;

    @Test
    @DisplayName("Empty list of deployments returned when no engine exists")
    void givenNoEngine_whenFindAll_thenEmptyListReturned() {
        //when
        List<DeploymentData> deployments = deploymentService.findAll(new DeploymentLoadContext());

        //then
        assertThat(deployments).isEmpty();
    }

    @Test
    @DisplayName("Empty list of deployment resources returned when no engine exists")
    void givenNoEngine_whenGetDeploymentResources_thenEmptyListReturned() {
        //when
        List<DeploymentResource> resources = deploymentService.getDeploymentResources("deploymentId");

        //then
        assertThat(resources).isEmpty();
    }

    @Test
    @DisplayName("Zero returned as deployments count when no engine exists")
    void givenNoEngine_whenGetCount_thenZeroReturned() {
        //when
        long count = deploymentService.getCount(null);

        //then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Null returned for deployment when no engine exists")
    void givenNoEngine_whenFindById_thenNullReturned() {
        //when
        DeploymentData deployment = deploymentService.findById("deploymentId");

        //then
        assertThat(deployment).isNull();
    }

    @Test
    @DisplayName("Null returned for deployment resource data when no engine exists")
    void givenNoEngine_whenGetDeploymentResourceData_thenNullReturned() {
        //when
        Resource resourceData = deploymentService.getDeploymentResourceData("deploymentId", "resourceId");

        //then
        assertThat(resourceData).isNull();
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when create deployment if no engine exists")
    void givenNoEngine_whenCreateDeployment_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> deploymentService.createDeployment(new DeploymentContext()))
                .isInstanceOf(EngineNotSelectedException.class);
    }

    @Test
    @DisplayName("EngineNotSelectedException thrown when delete deployment if no engine exists")
    void givenNoEngine_whenDeleteById_thenEngineNotSelectedExceptionThrown() {
        //when and then
        assertThatThrownBy(() -> deploymentService.deleteById("deploymentId", true, true, true))
                .isInstanceOf(EngineNotSelectedException.class);
    }
}
