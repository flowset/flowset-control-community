package io.flowset.control.service.processinstance;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@WithRunningEngine
public class Camunda7MigrationServiceTest extends AbstractCamunda7IntegrationTest {
    @RunningEngine
    static Camunda7Container<?> camunda7;

    @Autowired
    MigrationService migrationService;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("EngineConnectionFailedException thrown when validate definition migration plan if engine is not available")
    void givenTwoProcessVersionsAndNotAvailableEngine_whenValidateMigrationPlan_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        List<String> visitPlanningProcessVersions = sampleDataManager.getDeployedProcessVersions("visitPlanning");
        String srcProcessVersion = visitPlanningProcessVersions.get(0);
        String targetProcessVersion = visitPlanningProcessVersions.get(1);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> migrationService.validateMigrationOfProcessInstances(srcProcessVersion, targetProcessVersion))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("No errors when create a process definition migration plan")
    void givenTwoProcessVersions_whenValidateMigrationPlan_thenNoErrorReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        List<String> visitPlanningProcessVersions = sampleDataManager.getDeployedProcessVersions("visitPlanning");
        String srcProcessVersion = visitPlanningProcessVersions.get(0);
        String targetProcessVersion = visitPlanningProcessVersions.get(1);

        //when
        List<String> migrationValidationErrors = migrationService.validateMigrationOfProcessInstances(srcProcessVersion, targetProcessVersion);

        //then
        assertThat(migrationValidationErrors).isEmpty();
    }

    @Test
    @DisplayName("EngineConnectionFailedException thrown when validate instance migration plan if engine is not available")
    void givenTwoProcessVersionsAndInstanceAndNotAvailableEngine_whenValidateMigrationPlan_thenExceptionThrown() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        List<String> visitPlanningProcessVersions = sampleDataManager.getDeployedProcessVersions("visitPlanning");

        String instanceId = sampleDataManager.getStartedInstances("visitPlanning").get(0);
        String targetProcessVersion = visitPlanningProcessVersions.get(1);

        camunda7.stop();

        //when and then
        assertThatThrownBy(() -> migrationService.validateMigrationOfSingleProcessInstance(instanceId, targetProcessVersion))
                .isInstanceOf(EngineConnectionFailedException.class);
    }

    @Test
    @DisplayName("No errors when create a process instance migration plan")
    void givenTwoProcessVersionsAndInstance_whenValidateMigrationPlan_thenNoErrorReturned() {
        //given
        CamundaSampleDataManager sampleDataManager = applicationContext.getBean(CamundaSampleDataManager.class, camunda7)
                .deploy("test_support/testVisitPlanningV1.bpmn")
                .startByKey("visitPlanning")
                .deploy("test_support/testVisitPlanningV2.bpmn");

        List<String> visitPlanningProcessVersions = sampleDataManager.getDeployedProcessVersions("visitPlanning");

        String instanceId = sampleDataManager.getStartedInstances("visitPlanning").get(0);
        String targetProcessVersion = visitPlanningProcessVersions.get(1);

        //when
        List<String> migrationValidationErrors = migrationService.validateMigrationOfSingleProcessInstance(instanceId, targetProcessVersion);

        //then
        assertThat(migrationValidationErrors).isEmpty();
    }
}
