/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.engine;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.service.activity.ActivityService;
import io.flowset.control.service.activity.impl.Camunda7ActivityServiceImpl;
import io.flowset.control.service.activity.impl.NoopActivityServiceImpl;
import io.flowset.control.service.batch.BatchService;
import io.flowset.control.service.batch.impl.Camunda7BatchServiceImpl;
import io.flowset.control.service.batch.impl.NoopBatchServiceImpl;
import io.flowset.control.service.dashboard.DashboardService;
import io.flowset.control.service.dashboard.impl.Camunda7DashboardServiceImpl;
import io.flowset.control.service.dashboard.impl.NoopDashboardServiceImpl;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.decisiondefinition.impl.Camunda7DecisionDefinitionServiceImpl;
import io.flowset.control.service.decisiondefinition.impl.NoopDecisionDefinitionServiceImpl;
import io.flowset.control.service.decisioninstance.DecisionInstanceService;
import io.flowset.control.service.decisioninstance.impl.Camunda7DecisionInstanceServiceImpl;
import io.flowset.control.service.decisioninstance.impl.NoopDecisionInstanceServiceImpl;
import io.flowset.control.service.deployment.DeploymentService;
import io.flowset.control.service.deployment.impl.Camunda7DeploymentServiceImpl;
import io.flowset.control.service.deployment.impl.NoopDeploymentServiceImpl;
import io.flowset.control.service.engine.impl.Camunda7EngineConnectionProvider;
import io.flowset.control.service.engine.impl.NoopEngineConnectionProvider;
import io.flowset.control.service.externaltask.ExternalTaskService;
import io.flowset.control.service.externaltask.impl.Camunda7ExternalTaskServiceImpl;
import io.flowset.control.service.incident.IncidentService;
import io.flowset.control.service.incident.impl.Camunda7IncidentServiceImpl;
import io.flowset.control.service.incident.impl.NoopIncidentServiceImpl;
import io.flowset.control.service.job.JobService;
import io.flowset.control.service.job.impl.Camunda7JobServiceImpl;
import io.flowset.control.service.job.impl.NoopJobServiceImpl;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.flowset.control.service.processdefinition.impl.Camunda7ProcessDefinitionServiceImpl;
import io.flowset.control.service.processdefinition.impl.NoopProcessDefinitionServiceImpl;
import io.flowset.control.service.processinstance.MigrationService;
import io.flowset.control.service.processinstance.ProcessInstanceService;
import io.flowset.control.service.processinstance.impl.Camunda7MigrationServiceImpl;
import io.flowset.control.service.processinstance.impl.Camunda7ProcessInstanceServiceImpl;
import io.flowset.control.service.processinstance.impl.NoopMigrationServiceImpl;
import io.flowset.control.service.processinstance.impl.NoopProcessInstanceServiceImpl;
import io.flowset.control.service.usertask.UserTaskService;
import io.flowset.control.service.usertask.impl.Camunda7UserTaskServiceImpl;
import io.flowset.control.service.usertask.impl.NoopUserTaskServiceImpl;
import io.flowset.control.service.variable.VariableService;
import io.flowset.control.service.variable.impl.Camunda7VariableServiceImpl;
import io.flowset.control.service.variable.impl.NoopVariableServiceImpl;
import io.flowset.control.test_support.AbstractIntegrationTest;
import io.flowset.control.test_support.AuthenticatedAsAdmin;
import io.flowset.control.test_support.ControlTestDataCreator;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class EngineOperationServiceProviderTest extends AbstractIntegrationTest {

    @Autowired
    EngineOperationServiceProvider engineOperationServiceProvider;
    @Autowired
    EngineService engineService;
    @Autowired
    ControlTestDataCreator controlTestDataCreator;
    @Autowired
    DataManager dataManager;

    @AfterEach
    void tearDown() {
        resetSelectedEngine();
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 activity service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetActivityService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        ActivityService service = engineOperationServiceProvider.getActivityService();

        //then
        assertThat(service).isInstanceOf(Camunda7ActivityServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 batch service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetBatchService_thenCamunda7ServiceReturned(EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        BatchService service = engineOperationServiceProvider.getBatchService();

        //then
        assertThat(service).isInstanceOf(Camunda7BatchServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 dashboard service returned for Camunda 7-compatible engine")
    void givenCamunda7CompatibleEngine_whenGetDashboardService_thenCamunda7ServiceReturned(EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engine = dataManager.save(engine);

        //when
        DashboardService service = engineOperationServiceProvider.getDashboardService(engine);

        //then
        assertThat(service).isInstanceOf(Camunda7DashboardServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 decision definition service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetDecisionDefinitionService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        DecisionDefinitionService service = engineOperationServiceProvider.getDecisionDefinitionService();

        //then
        assertThat(service).isInstanceOf(Camunda7DecisionDefinitionServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 decision instance service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetDecisionInstanceService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        DecisionInstanceService service = engineOperationServiceProvider.getDecisionInstanceService();

        //then
        assertThat(service).isInstanceOf(Camunda7DecisionInstanceServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 deployment service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetDeploymentService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        DeploymentService service = engineOperationServiceProvider.getDeploymentService();

        //then
        assertThat(service).isInstanceOf(Camunda7DeploymentServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 external task service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetExternalTaskService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        ExternalTaskService service = engineOperationServiceProvider.getExternalTaskService();

        //then
        assertThat(service).isInstanceOf(Camunda7ExternalTaskServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 incident service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetIncidentService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        IncidentService service = engineOperationServiceProvider.getIncidentService();

        //then
        assertThat(service).isInstanceOf(Camunda7IncidentServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 job service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetJobService_thenCamunda7ServiceReturned(EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        JobService service = engineOperationServiceProvider.getJobService();

        //then
        assertThat(service).isInstanceOf(Camunda7JobServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 migration service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetMigrationService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        MigrationService service = engineOperationServiceProvider.getMigrationService();

        //then
        assertThat(service).isInstanceOf(Camunda7MigrationServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 process definition service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetProcessDefinitionService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        ProcessDefinitionService service = engineOperationServiceProvider.getProcessDefinitionService();

        //then
        assertThat(service).isInstanceOf(Camunda7ProcessDefinitionServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 process instance service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetProcessInstanceService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        ProcessInstanceService service = engineOperationServiceProvider.getProcessInstanceService();

        //then
        assertThat(service).isInstanceOf(Camunda7ProcessInstanceServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 user task service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetUserTaskService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        UserTaskService service = engineOperationServiceProvider.getUserTaskService();

        //then
        assertThat(service).isInstanceOf(Camunda7UserTaskServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 variable service returned for Camunda 7-compatible engine")
    void givenSelectedCamunda7CompatibleEngine_whenGetVariableService_thenCamunda7ServiceReturned(
            EngineType engineType) {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engine.setType(engineType);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        VariableService service = engineOperationServiceProvider.getVariableService();

        //then
        assertThat(service).isInstanceOf(Camunda7VariableServiceImpl.class);
    }

    @Test
    @DisplayName("Dashboard service chosen by the given engine, not by the selected one")
    void givenSelectedEngine_whenGetDashboardServiceForNoEngine_thenNoopServiceReturned() {
        //given
        BpmEngine engine = controlTestDataCreator.createRandomBpmEngine("Engine", true);
        engineService.setSelectedEngine(dataManager.save(engine));

        //when
        DashboardService service = engineOperationServiceProvider.getDashboardService(null);

        //then
        assertThat(service).isInstanceOf(NoopDashboardServiceImpl.class);
    }

    @ParameterizedTest
    @EnumSource(value = EngineType.class, names = {"CAMUNDA_7", "OPERATON", "FLUXNOVA", "CIB_SEVEN"})
    @DisplayName("Camunda 7 connection provider returned for Camunda 7-compatible engine type")
    void givenCamunda7CompatibleEngineType_whenGetConnectionProvider_thenCamunda7ProviderReturned(
            EngineType engineType) {
        //when
        EngineConnectionProvider connectionProvider = engineOperationServiceProvider.getConnectionProvider(engineType);

        //then
        assertThat(connectionProvider).isInstanceOf(Camunda7EngineConnectionProvider.class);
    }

    @Test
    @DisplayName("Noop connection provider returned for engine without type")
    void givenNoEngineType_whenGetConnectionProvider_thenNoopProviderReturned() {
        //when
        EngineConnectionProvider connectionProvider = engineOperationServiceProvider.getConnectionProvider(null);

        //then
        assertThat(connectionProvider).isInstanceOf(NoopEngineConnectionProvider.class);
    }

    @Test
    @DisplayName("Noop activity service returned when no engine exists")
    void givenNoEngine_whenGetActivityService_thenNoopServiceReturned() {
        //when
        ActivityService service = engineOperationServiceProvider.getActivityService();

        //then
        assertThat(service).isInstanceOf(NoopActivityServiceImpl.class);
    }

    @Test
    @DisplayName("Noop batch service returned when no engine exists")
    void givenNoEngine_whenGetBatchService_thenNoopServiceReturned() {
        //when
        BatchService service = engineOperationServiceProvider.getBatchService();

        //then
        assertThat(service).isInstanceOf(NoopBatchServiceImpl.class);
    }

    @Test
    @DisplayName("Noop decision definition service returned when no engine exists")
    void givenNoEngine_whenGetDecisionDefinitionService_thenNoopServiceReturned() {
        //when
        DecisionDefinitionService service = engineOperationServiceProvider.getDecisionDefinitionService();

        //then
        assertThat(service).isInstanceOf(NoopDecisionDefinitionServiceImpl.class);
    }

    @Test
    @DisplayName("Noop decision instance service returned when no engine exists")
    void givenNoEngine_whenGetDecisionInstanceService_thenNoopServiceReturned() {
        //when
        DecisionInstanceService service = engineOperationServiceProvider.getDecisionInstanceService();

        //then
        assertThat(service).isInstanceOf(NoopDecisionInstanceServiceImpl.class);
    }

    @Test
    @DisplayName("Noop deployment service returned when no engine exists")
    void givenNoEngine_whenGetDeploymentService_thenNoopServiceReturned() {
        //when
        DeploymentService service = engineOperationServiceProvider.getDeploymentService();

        //then
        assertThat(service).isInstanceOf(NoopDeploymentServiceImpl.class);
    }

    @Test
    @DisplayName("Noop incident service returned when no engine exists")
    void givenNoEngine_whenGetIncidentService_thenNoopServiceReturned() {
        //when
        IncidentService service = engineOperationServiceProvider.getIncidentService();

        //then
        assertThat(service).isInstanceOf(NoopIncidentServiceImpl.class);
    }

    @Test
    @DisplayName("Noop job service returned when no engine exists")
    void givenNoEngine_whenGetJobService_thenNoopServiceReturned() {
        //when
        JobService service = engineOperationServiceProvider.getJobService();

        //then
        assertThat(service).isInstanceOf(NoopJobServiceImpl.class);
    }

    @Test
    @DisplayName("Noop migration service returned when no engine exists")
    void givenNoEngine_whenGetMigrationService_thenNoopServiceReturned() {
        //when
        MigrationService service = engineOperationServiceProvider.getMigrationService();

        //then
        assertThat(service).isInstanceOf(NoopMigrationServiceImpl.class);
    }

    @Test
    @DisplayName("Noop process definition service returned when no engine exists")
    void givenNoEngine_whenGetProcessDefinitionService_thenNoopServiceReturned() {
        //when
        ProcessDefinitionService service = engineOperationServiceProvider.getProcessDefinitionService();

        //then
        assertThat(service).isInstanceOf(NoopProcessDefinitionServiceImpl.class);
    }

    @Test
    @DisplayName("Noop process instance service returned when no engine exists")
    void givenNoEngine_whenGetProcessInstanceService_thenNoopServiceReturned() {
        //when
        ProcessInstanceService service = engineOperationServiceProvider.getProcessInstanceService();

        //then
        assertThat(service).isInstanceOf(NoopProcessInstanceServiceImpl.class);
    }

    @Test
    @DisplayName("Noop user task service returned when no engine exists")
    void givenNoEngine_whenGetUserTaskService_thenNoopServiceReturned() {
        //when
        UserTaskService service = engineOperationServiceProvider.getUserTaskService();

        //then
        assertThat(service).isInstanceOf(NoopUserTaskServiceImpl.class);
    }

    @Test
    @DisplayName("Noop variable service returned when no engine exists")
    void givenNoEngine_whenGetVariableService_thenNoopServiceReturned() {
        //when
        VariableService service = engineOperationServiceProvider.getVariableService();

        //then
        assertThat(service).isInstanceOf(NoopVariableServiceImpl.class);
    }

    @Test
    @DisplayName("External task service not available when no engine exists")
    void givenNoEngine_whenGetExternalTaskService_thenExceptionThrown() {
        //when and then
        assertThatThrownBy(engineOperationServiceProvider::getExternalTaskService)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No selected engine type found");
    }
}
