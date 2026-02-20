/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an ability to prepare data in the provided Camunda 7 engine container, e.g. deploy process definition and start process instance(s) for it.
 */
@Scope("prototype")
@Component("control_CamundaSampleDataManager")
public class CamundaSampleDataManager {
    private static final Logger log = LoggerFactory.getLogger(CamundaSampleDataManager.class);

    private final Camunda7Container<?> camunda7;

    private CamundaRestTestHelper camundaRestTestHelper;

    private final Map<String, List<String>> processInstanceByProcessKey = new HashMap<>();
    private final Map<String, List<String>> deployedProcessesByKey = new HashMap<>();

    private final Map<String, List<String>> deployedDecisionsByKey = new HashMap<>();

    public CamundaSampleDataManager(Camunda7Container<?> camunda7) {
        this.camunda7 = camunda7;
    }

    @Autowired
    public void setCamundaRestTestHelper(CamundaRestTestHelper camundaRestTestHelper) {
        this.camundaRestTestHelper = camundaRestTestHelper;
    }

    /**
     * Deploys a provided resource (e.g. BPMN 2.0 XML) located in the classpath to engine container.
     *
     * @param resourcePath a resource to deploy in engine.
     * @return current instance of bean
     */
    public CamundaSampleDataManager deploy(String resourcePath) {
        DeploymentResultDto deployment = camundaRestTestHelper.createDeployment(camunda7, resourcePath);
        Map<String, ProcessDefinitionDto> deployedProcessDefinitions = deployment.getDeployedProcessDefinitions();

        if (deployedProcessDefinitions != null) {
            log.info("Deploy {} processes from the file by path {}", deployedProcessDefinitions.size(), resourcePath);

            deployedProcessDefinitions.forEach((processDefinitionId, processDefinitionDto) -> {
                List<String> ids = deployedProcessesByKey.getOrDefault(processDefinitionDto.getKey(), new ArrayList<>());
                ids.add(processDefinitionId);

                deployedProcessesByKey.put(processDefinitionDto.getKey(), ids);
            });
        }

        Map<String, DecisionDefinitionDto> deployedDecisionDefinitions = deployment.getDeployedDecisionDefinitions();
        if (deployedDecisionDefinitions != null) {
            log.info("Deploy {} decisions from the file by path {}", deployedDecisionDefinitions.size(), resourcePath);
            deployedDecisionDefinitions.forEach((decisionDefinitionId, decisionDefinitionDto) -> {
                List<String> ids = deployedDecisionsByKey.getOrDefault(decisionDefinitionDto.getKey(), new ArrayList<>());
                ids.add(decisionDefinitionId);

                deployedDecisionsByKey.put(decisionDefinitionDto.getKey(), ids);
            });
        }

        return this;
    }

    /**
     * Starts a process instance with the provided data and key of the process definition deployed in the engine container.
     *
     * @param processKey a key of process definition deployed in the engine container
     * @param dto        a data to start process instance
     * @return current instance of bean
     * @see #deploy(String)
     */
    public CamundaSampleDataManager startByKey(String processKey, StartProcessDto dto) {
        List<String> runningInstances = processInstanceByProcessKey.getOrDefault(processKey, new ArrayList<>());

        RuntimeProcessInstanceDto runtimeProcessInstanceDto = camundaRestTestHelper.startProcessByKey(camunda7, processKey, dto);
        if (runtimeProcessInstanceDto != null) {
            log.info("Started instance (id: '{}') for process key {}", runtimeProcessInstanceDto.getId(), processKey);
            runningInstances.add(runtimeProcessInstanceDto.getId());
            processInstanceByProcessKey.put(processKey, runningInstances);
        }

        return this;
    }

    /**
     * Starts the specified number of process instances with the provided data and key of
     * the process definition deployed in the engine container.
     *
     * @param processKey a key of process definition deployed in the engine container
     * @param dto        a data to start process instance
     * @param count      a count of process instances that should be started
     * @return current instance of bean
     * @see #deploy(String)
     */
    public CamundaSampleDataManager startByKey(String processKey, StartProcessDto dto, long count) {
        for (int i = 0; i < count; i++) {
            startByKey(processKey, dto);
        }
        return this;
    }

    /**
     * Starts the specified number of process instances with the provided key of the process definition deployed in the engine container.
     *
     * @param processKey a key of process definition deployed in the engine container
     * @param count      a count of process instances that should be started
     * @return current instance of bean
     * @see #deploy(String)
     */
    public CamundaSampleDataManager startByKey(String processKey, long count) {
        return startByKey(processKey, new StartProcessDto(), count);
    }

    /**
     * Starts a process instance with the provided key of the process definition deployed in the engine container.
     *
     * @param processKey a key of process definition deployed in the engine container
     * @return current instance of bean
     * @see #deploy(String)
     */
    public CamundaSampleDataManager startByKey(String processKey) {
        return startByKey(processKey, new StartProcessDto());
    }

    /**
     * Waits until all active tasks for previously running instances of the process have been processed by the BPM engine.
     * Periodically requests a count of active jobs exiting in BPM engine. If no active jobs exists or max attempts are done, the waiting is completed.
     *
     * @return current instance of bean
     */
    public CamundaSampleDataManager waitJobsExecution() {
        boolean activeJobsExists;
        List<String> processInstances = processInstanceByProcessKey.values().stream().flatMap(List::stream).toList();

        int attempts = 0;
        do {
            attempts++;
            activeJobsExists = camundaRestTestHelper.activeJobsExists(camunda7, processInstances);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            if (attempts > 100) { //prevent infinite loop
                break;
            }
        } while (activeJobsExists);

        return this;
    }

    /**
     * Sets retries as one for failed jobs for previously running instances.
     *
     * @return current instance of bean
     */
    public CamundaSampleDataManager retryFailedJobs() {
        List<String> processInstances = processInstanceByProcessKey.values().stream().flatMap(List::stream).toList();

        List<JobDto> failedJobs = camundaRestTestHelper.getFailedJobs(camunda7, processInstances);
        for (JobDto failedJob : failedJobs) {
            camundaRestTestHelper.setJobRetries(camunda7, failedJob.getId(), 1);
        }

        return this;
    }

    /**
     * Sets retries as one for failed jobs for previously running instances of the processes with the specified key.
     *
     * @return current instance of bean
     */
    public CamundaSampleDataManager retryFailedJobs(String processKey) {
        List<String> processInstances = processInstanceByProcessKey.get(processKey);

        List<JobDto> failedJobs = camundaRestTestHelper.getFailedJobs(camunda7, processInstances);
        for (JobDto failedJob : failedJobs) {
            camundaRestTestHelper.setJobRetries(camunda7, failedJob.getId(), 1);
            log.info("Update job retries for job in the process {}", processKey);
        }

        return this;
    }

    /**
     * Suspends a deployed process definition with the specified key.
     *
     * @param processKey         a process definition
     * @param includingInstances whether suspend related instances as well
     * @return current instance of bean
     */
    public CamundaSampleDataManager suspendByKey(String processKey, boolean includingInstances) {
        camundaRestTestHelper.suspendProcessByKey(camunda7, processKey, includingInstances);
        return this;
    }

    public List<String> getDeployedProcessVersions(String key) {
        return deployedProcessesByKey.get(key);
    }

    public List<String> getDeployedDecisionVersions(String key) {
        return deployedDecisionsByKey.get(key);
    }

    public List<String> getStartedInstances(String processKey) {
        return processInstanceByProcessKey.getOrDefault(processKey, new ArrayList<>());
    }
}
