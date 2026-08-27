/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.test_support.camunda7.dto.request.HandleFailureDto;
import io.flowset.control.test_support.camunda7.dto.request.JobListRequestDto;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.UserTaskListRequestDto;
import io.flowset.control.test_support.camunda7.dto.response.*;
import io.flowset.control.test_support.engine.HasRunningEngineData;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides an ability to prepare data in the provided Camunda 7 engine container, e.g. deploy process definition and start process instance(s) for it.
 */
@Scope("prototype")
@Component("control_CamundaSampleDataManager")
public class CamundaSampleDataManager {
    private static final Logger log = LoggerFactory.getLogger(CamundaSampleDataManager.class);

    private final HasRunningEngineData camunda7;

    private CamundaRestTestHelper camundaRestTestHelper;

    private final Map<String, List<String>> processInstanceByProcessKey = new HashMap<>();
    private final Map<String, List<String>> deployedProcessesByKey = new HashMap<>();

    private final Map<String, List<String>> deployedDecisionsByKey = new HashMap<>();

    public CamundaSampleDataManager(HasRunningEngineData camunda7) {
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
     * Returns a list of user tasks related to the started instances of the process with the specified key.
     *
     * @param processKey a key of a process by which instances were started
     * @return a list of user task ids
     */
    public List<String> getUserTasksByKey(String processKey) {
        List<String> processInstances = processInstanceByProcessKey.get(processKey);
        if (CollectionUtils.isEmpty(processInstances)) {
            return Collections.emptyList();
        }
        return camundaRestTestHelper.getRuntimeUserTasks(camunda7, new UserTaskListRequestDto()
                .setProcessInstanceIdIn(processInstances));
    }

    /**
     * Returns a list of incidents related to the started instances of the process with the specified key.
     *
     * @param processKey a key of a process by which instances were started
     * @return a list of incident ids
     */
    public List<String> getIncidentsByKey(String processKey) {
        List<String> processInstances = processInstanceByProcessKey.get(processKey);
        if (CollectionUtils.isEmpty(processInstances)) {
            return Collections.emptyList();
        }
        List<String> incidentIds = new ArrayList<>();
        for (String processInstanceId : processInstances) {
            List<String> incidentIdsForProcessInstance = camundaRestTestHelper.getIncidentIdsByInstanceId(camunda7, processInstanceId);
            incidentIds.addAll(incidentIdsForProcessInstance);
        }
        return incidentIds;
    }

    /**
     * Returns a list of external tasks related to the started instances of the process with the specified key.
     *
     * @param processKey a key of a process by which instances were started
     * @return a list of external task ids
     */
    public List<String> getExternalTasksByKey(String processKey) {
        List<String> processInstances = processInstanceByProcessKey.get(processKey);
        if (CollectionUtils.isEmpty(processInstances)) {
            return Collections.emptyList();
        }
        return camundaRestTestHelper.getExternalTaskIds(camunda7, processInstances);
    }

    /**
     * Returns a list of jobs related to the started instances of the process with the specified key.
     *
     * @param processKey a key of a process by which instances were started
     * @return a list of job ids
     */
    public List<String> getJobsByKey(String processKey) {
        List<String> processInstances = processInstanceByProcessKey.get(processKey);
        if (CollectionUtils.isEmpty(processInstances)) {
            return Collections.emptyList();
        }
        return camundaRestTestHelper.getJobIds(camunda7, new JobListRequestDto()
                .setProcessInstanceIds(processInstances));
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
     * Fails external tasks for previously running instances of the processes with the specified key.
     *
     * @param processKey a key of a process by which instances were started
     */
    public CamundaSampleDataManager failExternalTasksByKey(String processKey) {
        List<String> processInstanceIds = processInstanceByProcessKey.get(processKey);
        if (CollectionUtils.isEmpty(processInstanceIds)) {
            return this;
        }
        List<String> externalTaskIds = camundaRestTestHelper.getExternalTaskIds(camunda7, processInstanceIds);
        externalTaskIds.forEach(externalTaskId -> camundaRestTestHelper.failExternalTask(camunda7, externalTaskId,
                HandleFailureDto.builder()
                        .errorMessage("Service not available")
                        .errorDetails("I/O exception occurred during service connection")
                        .retries(0)
                        .workerId("test-worker")
                        .build()));
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

    /**
     * Returns a list of deployed process version identifiers for the specified key.
     *
     * @param key a process definition key
     * @return a list of deployed process version identifiers
     */
    public List<String> getDeployedProcessVersions(String key) {
        return deployedProcessesByKey.get(key);
    }

    /**
     * Returns a list of deployed decision version identifiers for the specified key.
     *
     * @param key a decision definition key
     * @return a list of deployed decision version identifiers
     */
    public List<String> getDeployedDecisionVersions(String key) {
        return deployedDecisionsByKey.get(key);
    }

    /**
     * Returns a list of started instances for the specified process key.
     *
     * @param processKey a process definition key
     * @return a list of started instance identifiers
     */
    public List<String> getStartedInstances(String processKey) {
        return processInstanceByProcessKey.getOrDefault(processKey, new ArrayList<>());
    }

    /**
     * Returns a list of evaluated decision instances for the specified decision key.
     *
     * @param decisionKey a decision definition key
     * @return a list of started instance identifiers
     */
    public List<String> getDecisionInstances(String decisionKey) {
        if (!deployedDecisionsByKey.containsKey(decisionKey)) {
            return Collections.emptyList();
        }
        return camundaRestTestHelper.getDecisionInstancesByKey(camunda7, decisionKey);
    }

    /**
     * Evaluates the deployed decision by key without an enclosing process. Produces a historic decision instance
     * with no {@code processInstanceId} or {@code processDefinitionId}.
     *
     * @param decisionKey a key of a decision deployed in the engine container
     * @return current instance of bean
     * @see #deploy(String)
     */
    public CamundaSampleDataManager evaluateDecisionByKey(String decisionKey) {
        camundaRestTestHelper.evaluateDecisionByKey(camunda7, decisionKey, Map.of());
        return this;
    }
}
