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
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Picks the implementation of an engine service by the type of the selected engine. When no engine exists, the
 * services a view can reach return their {@code Noop*} implementation; the others throw
 * {@link IllegalStateException}.
 */
@Component("control_EngineOperationServiceProvider")
public class EngineOperationServiceProvider {

    protected final ApplicationContext applicationContext;
    protected final EngineService engineService;

    protected final Map<EngineType, EngineConnectionProvider> connectionProviderMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, ActivityService> activityServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, BatchService> batchServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, DashboardService> dashboardServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, DecisionDefinitionService> decisionDefinitionServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, DecisionInstanceService> decisionInstanceServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, DeploymentService> deploymentServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, ExternalTaskService> externalTaskServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, IncidentService> incidentServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, JobService> jobServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, MigrationService> migrationServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, ProcessDefinitionService> processDefinitionServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, ProcessInstanceService> processInstanceServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, UserTaskService> userTaskServiceMap = new ConcurrentHashMap<>();
    protected final Map<EngineType, VariableService> variableServiceMap = new ConcurrentHashMap<>();

    public EngineOperationServiceProvider(ApplicationContext applicationContext, EngineService engineService) {
        this.applicationContext = applicationContext;
        this.engineService = engineService;
    }

    @PostConstruct
    protected void init() {
        initCamunda7Services();
    }

    public EngineConnectionProvider getConnectionProvider(@Nullable EngineType engineType) {
        return getService(connectionProviderMap, engineType, EngineConnectionProvider.class,
                NoopEngineConnectionProvider.class);
    }
    public ActivityService getActivityService() {
        return getService(activityServiceMap, engineService.getSelectedEngineType(), ActivityService.class,
                NoopActivityServiceImpl.class);
    }

    public BatchService getBatchService() {
        return getService(batchServiceMap, engineService.getSelectedEngineType(), BatchService.class,
                NoopBatchServiceImpl.class);
    }

    public DashboardService getDashboardService(@Nullable BpmEngine engine) {
        EngineType engineType = engine != null ? engine.getType() : null;
        return getService(dashboardServiceMap, engineType, DashboardService.class, NoopDashboardServiceImpl.class);
    }

    public DecisionDefinitionService getDecisionDefinitionService() {
        return getService(decisionDefinitionServiceMap, engineService.getSelectedEngineType(),
                DecisionDefinitionService.class, NoopDecisionDefinitionServiceImpl.class);
    }

    public DecisionInstanceService getDecisionInstanceService() {
        return getService(decisionInstanceServiceMap, engineService.getSelectedEngineType(),
                DecisionInstanceService.class, NoopDecisionInstanceServiceImpl.class);
    }

    public DeploymentService getDeploymentService() {
        return getService(deploymentServiceMap, engineService.getSelectedEngineType(), DeploymentService.class,
                NoopDeploymentServiceImpl.class);
    }

    public ExternalTaskService getExternalTaskService() {
        return getService(externalTaskServiceMap, engineService.getSelectedEngineType(), ExternalTaskService.class);
    }

    public IncidentService getIncidentService() {
        return getService(incidentServiceMap, engineService.getSelectedEngineType(), IncidentService.class,
                NoopIncidentServiceImpl.class);
    }

    public JobService getJobService() {
        return getService(jobServiceMap, engineService.getSelectedEngineType(), JobService.class,
                NoopJobServiceImpl.class);
    }

    public MigrationService getMigrationService() {
        return getService(migrationServiceMap, engineService.getSelectedEngineType(), MigrationService.class,
                NoopMigrationServiceImpl.class);
    }

    public ProcessDefinitionService getProcessDefinitionService() {
        return getService(processDefinitionServiceMap, engineService.getSelectedEngineType(),
                ProcessDefinitionService.class, NoopProcessDefinitionServiceImpl.class);
    }

    public ProcessInstanceService getProcessInstanceService() {
        return getService(processInstanceServiceMap, engineService.getSelectedEngineType(),
                ProcessInstanceService.class, NoopProcessInstanceServiceImpl.class);
    }

    public UserTaskService getUserTaskService() {
        return getService(userTaskServiceMap, engineService.getSelectedEngineType(), UserTaskService.class,
                NoopUserTaskServiceImpl.class);
    }

    public VariableService getVariableService() {
        return getService(variableServiceMap, engineService.getSelectedEngineType(), VariableService.class,
                NoopVariableServiceImpl.class);
    }

    protected void initCamunda7Services() {
        EngineType key = EngineType.CAMUNDA_7;
        connectionProviderMap.put(key, applicationContext.getBean(Camunda7EngineConnectionProvider.class));
        activityServiceMap.put(key, applicationContext.getBean(Camunda7ActivityServiceImpl.class));
        batchServiceMap.put(key, applicationContext.getBean(Camunda7BatchServiceImpl.class));
        dashboardServiceMap.put(key, applicationContext.getBean(Camunda7DashboardServiceImpl.class));
        decisionDefinitionServiceMap.put(key, applicationContext.getBean(Camunda7DecisionDefinitionServiceImpl.class));
        decisionInstanceServiceMap.put(key, applicationContext.getBean(Camunda7DecisionInstanceServiceImpl.class));
        deploymentServiceMap.put(key, applicationContext.getBean(Camunda7DeploymentServiceImpl.class));
        externalTaskServiceMap.put(key, applicationContext.getBean(Camunda7ExternalTaskServiceImpl.class));
        incidentServiceMap.put(key, applicationContext.getBean(Camunda7IncidentServiceImpl.class));
        jobServiceMap.put(key, applicationContext.getBean(Camunda7JobServiceImpl.class));
        migrationServiceMap.put(key, applicationContext.getBean(Camunda7MigrationServiceImpl.class));
        processDefinitionServiceMap.put(key, applicationContext.getBean(Camunda7ProcessDefinitionServiceImpl.class));
        processInstanceServiceMap.put(key, applicationContext.getBean(Camunda7ProcessInstanceServiceImpl.class));
        userTaskServiceMap.put(key, applicationContext.getBean(Camunda7UserTaskServiceImpl.class));
        variableServiceMap.put(key, applicationContext.getBean(Camunda7VariableServiceImpl.class));
    }

    /**
     * Resolves the key of the service maps for the given engine type. Camunda 7-compatible engines expose the same
     * REST API, so they are served by the same implementations stored under {@link EngineType#CAMUNDA_7}.
     *
     * @param engineType the engine type
     * @return {@link EngineType#CAMUNDA_7} for a Camunda 7-compatible type, otherwise the given type
     */
    protected EngineType resolveEngineTypeKey(EngineType engineType) {
        return engineType.isCamunda7Compatible() ? EngineType.CAMUNDA_7 : engineType;
    }

    protected <T> T getService(Map<EngineType, T> serviceMap, @Nullable EngineType engineType, Class<T> serviceClass,
                               Class<? extends T> noopServiceClass) {
        if (engineType == null) {
            return applicationContext.getBean(noopServiceClass);
        }
        return getService(serviceMap, engineType, serviceClass);
    }

    protected <T> T getService(Map<EngineType, T> serviceMap, @Nullable EngineType engineType,
                               Class<T> serviceClass) {
        if (engineType == null) {
            throw new IllegalStateException("No selected engine type found");
        }
        T service = serviceMap.get(resolveEngineTypeKey(engineType));
        if (service == null) {
            throw new IllegalStateException("No " + serviceClass.getSimpleName() + " found for engine type "
                    + engineType);
        }
        return service;
    }
}
