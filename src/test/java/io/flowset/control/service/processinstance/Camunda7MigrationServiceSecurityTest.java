/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import io.flowset.control.entity.batch.BatchData;
import io.flowset.control.test_support.RunningEngine;
import io.flowset.control.test_support.WithRunningEngine;
import io.flowset.control.test_support.camunda7.AbstractCamunda7IntegrationTest;
import io.flowset.control.test_support.camunda7.Camunda7Container;
import io.flowset.control.test_support.camunda7.CamundaRestTestHelper;
import io.flowset.control.test_support.camunda7.CamundaSampleDataManager;
import io.flowset.control.test_support.camunda7.dto.response.RuntimeProcessInstanceDto;
import io.flowset.control.test_support.security.AuthenticatedAsUser;
import io.flowset.control.test_support.security.WithTestUser;
import io.flowset.control.test_support.security.role.processdefinition.TestProcessDefinitionMigratePermissionRole;
import io.flowset.control.test_support.security.role.processinstance.TestProcessInstanceMigratePermissionRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WithRunningEngine
public class Camunda7MigrationServiceSecurityTest extends AbstractCamunda7IntegrationTest {

    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MigrationService migrationService;

    @Autowired
    CamundaRestTestHelper camundaRestTestHelper;

    @Test
    @DisplayName("Migrate single process instance when user has process instance migrate permission")
    @WithTestUser(username = "test-user-secured-process-instance-migrate",
            roles = TestProcessInstanceMigratePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-instance-migrate")
    void givenUserWithProcessInstanceMigratePermission_whenMigrateSingleProcessInstance_thenProcessInstanceMigrated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");
        List<String> processVersions = sampleDataManager.getDeployedProcessVersions("visitPlanning");
        String instanceId = sampleDataManager.getStartedInstances("visitPlanning").get(0);
        String targetProcessDefinitionId = processVersions.get(1);

        //when
        migrationService.migrateSingleProcessInstance(instanceId, targetProcessDefinitionId);

        //then
        RuntimeProcessInstanceDto runtimeInstance = camundaRestTestHelper.getRuntimeInstanceById(camunda7, instanceId);
        assertThat(runtimeInstance).isNotNull();
        assertThat(runtimeInstance.getDefinitionId()).isEqualTo(targetProcessDefinitionId);
    }

    @Test
    @DisplayName("Migrate all process instances when user has process definition migrate permission")
    @WithTestUser(username = "test-user-secured-process-definition-migrate",
            roles = TestProcessDefinitionMigratePermissionRole.class)
    @AuthenticatedAsUser(username = "test-user-secured-process-definition-migrate")
    void givenUserWithProcessDefinitionMigratePermission_whenMigrateAllProcessInstances_thenBatchCreated() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning", 2)
                .deploy("test_support/testVisitPlanningV2.bpmn");
        List<String> processVersions = sampleDataManager.getDeployedProcessVersions("visitPlanning");
        String sourceProcessDefinitionId = processVersions.get(0);
        String targetProcessDefinitionId = processVersions.get(1);

        //when
        BatchData batchData = migrationService.migrateAllProcessInstances(sourceProcessDefinitionId,
                targetProcessDefinitionId);
        camundaRestTestHelper.waitForBatchExecution(camunda7);

        //then
        assertThat(batchData).isNotNull();
        assertThat(batchData.getId()).isNotBlank();
        assertThat(camundaRestTestHelper.getRuntimeInstancesById(camunda7, sourceProcessDefinitionId)).isEmpty();
        assertThat(camundaRestTestHelper.getRuntimeInstancesById(camunda7, targetProcessDefinitionId)).hasSize(2);
    }
}
