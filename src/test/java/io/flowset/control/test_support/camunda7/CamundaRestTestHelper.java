/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.test_support.EngineTestContainerRestHelper;
import io.flowset.control.test_support.camunda7.dto.IdDto;
import io.flowset.control.test_support.camunda7.dto.request.*;
import io.flowset.control.test_support.camunda7.dto.response.*;
import io.flowset.control.test_support.engine.HasRunningEngineData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.camunda.community.rest.client.model.IncidentDto;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for working with REST API of running Camunda 7 and compatible engines.
 */
@Component("control_CamundaRestTestHelper")
public class CamundaRestTestHelper {
    private final EngineTestContainerRestHelper restHelper;

    public CamundaRestTestHelper(EngineTestContainerRestHelper restHelper) {
        this.restHelper = restHelper;
    }

    public DeploymentResultDto createDeployment(HasRunningEngineData camunda, String resourceClassPath) {
        String name = FilenameUtils.getName(resourceClassPath);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("deployment-name", name);
        parts.add(name, new ClassPathResource(resourceClassPath));

        return restHelper.postOne(camunda, "/deployment/create", parts, DeploymentResultDto.class);
    }

    /**
     * Iterates entries of the zip archive on the classpath and creates a separate Camunda deployment
     * for each non-directory entry.
     *
     * @param camunda           running engine
     * @param resourceClassPath classpath path of the {@code .zip} archive
     * @return one {@link DeploymentResultDto} per deployed file, in archive iteration order
     */
    public List<DeploymentResultDto> createDeploymentFromZip(HasRunningEngineData camunda, String resourceClassPath) {
        List<DeploymentResultDto> deployments = new ArrayList<>();
        try (InputStream in = new ClassPathResource(resourceClassPath).getInputStream();
             ZipInputStream zip = new ZipInputStream(in, StandardCharsets.UTF_8)) {

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String resourceName = FilenameUtils.getName(entry.getName());
                String content = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                deployments.add(createDeployment(camunda, resourceName, content));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read zip archive from classpath: " + resourceClassPath, e);
        }
        return deployments;
    }

    public DeploymentResultDto createDeployment(HasRunningEngineData camunda, String resourceName, String resourceContent) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("deployment-name", resourceName);
        parts.add(resourceName, new ByteArrayResource(resourceContent.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return resourceName;
            }
        });

        return restHelper.postOne(camunda, "/deployment/create", parts, DeploymentResultDto.class);
    }

    public RuntimeProcessInstanceDto findRuntimeInstance(HasRunningEngineData camunda, String processInstanceId) {
        return restHelper.getOne(camunda, "/process-instance/" + processInstanceId, RuntimeProcessInstanceDto.class);
    }

    public long getRunningProcessesCount(HasRunningEngineData camunda) {
        CountResultDto body = restHelper.getOne(camunda, "/process-instance/count", CountResultDto.class);

        return body != null ? body.getCount() : -1;
    }

    public RuntimeProcessInstanceDto startProcessByKey(HasRunningEngineData camunda, String processKey, StartProcessDto dto) {
        return restHelper.postOne(camunda, "/process-definition/key/" + processKey + "/start", dto, RuntimeProcessInstanceDto.class);
    }

    public void suspendInstanceById(HasRunningEngineData camunda, String instanceId) {
        SuspendRequestDto suspendRequestDto = new SuspendRequestDto();
        suspendRequestDto.setSuspended(true);

        restHelper.putVoid(camunda, "/process-instance/" + instanceId + "/suspended", suspendRequestDto);
    }

    public void suspendJobById(HasRunningEngineData camunda, String jobId) {
        SuspendRequestDto suspendRequestDto = new SuspendRequestDto();
        suspendRequestDto.setSuspended(true);

        restHelper.putVoid(camunda, "/job/" + jobId + "/suspended", suspendRequestDto);
    }

    public void suspendInstanceByProcessId(HasRunningEngineData camunda, String processId) {
        SuspendInstancesRequestDto suspendRequestDto = new SuspendInstancesRequestDto();
        suspendRequestDto.setSuspended(true);
        suspendRequestDto.setProcessDefinitionId(processId);

        restHelper.putVoid(camunda, "/process-instance/suspended", suspendRequestDto);
    }

    public void suspendProcessByKey(HasRunningEngineData camunda, String processKey, boolean includeInstances) {
        SuspendProcessRequestDto suspendRequestDto = new SuspendProcessRequestDto();
        suspendRequestDto.setSuspended(true);
        suspendRequestDto.setIncludeProcessInstances(includeInstances);

        restHelper.putVoid(camunda, "/process-definition/key/" + processKey + "/suspended", suspendRequestDto);
    }

    public BatchDto suspendInstancesAsync(HasRunningEngineData camunda, List<String> processInstanceIds) {
        return restHelper.postOne(camunda, "/process-instance/suspended-async",
                Map.of("processInstanceIds", processInstanceIds),
                BatchDto.class);
    }

    public BatchDto deleteInstancesAsync(HasRunningEngineData camunda, List<String> processInstanceIds) {
        return restHelper.postOne(camunda, "/process-instance/delete",
                Map.of("skipCustomListeners", false,
                        "processInstanceIds", processInstanceIds),
                BatchDto.class);
    }

    public HistoricProcessInstanceDto getHistoricProcessInstanceById(HasRunningEngineData camunda, String processInstanceId) {
        return restHelper.getOne(camunda, "/process-instance/" + processInstanceId, HistoricProcessInstanceDto.class);
    }

    public List<String> getDecisionInstancesByKey(HasRunningEngineData camunda, String decisionDefinitionKey) {
        return restHelper.getList(camunda, "/history/decision-instance?decisionDefinitionKey=" + decisionDefinitionKey, IdDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public void evaluateDecisionByKey(HasRunningEngineData camunda, String decisionKey, Map<String, Object> variables) {
        restHelper.postVoid(camunda, "/decision-definition/key/" + decisionKey + "/evaluate", Map.of("variables", variables));
    }

    public DecisionInstanceDto getDecisionInstanceById(HasRunningEngineData camunda, String decisionInstanceId) {
        return restHelper.getOne(camunda, "/history/decision-instance/" + decisionInstanceId, DecisionInstanceDto.class);
    }

    public void suspendProcessById(HasRunningEngineData camunda, String processKey,
                                   String processDefinitionId, boolean includeInstances) {
        SuspendProcessRequestDto suspendRequestDto = new SuspendProcessRequestDto();
        suspendRequestDto.setSuspended(true);
        suspendRequestDto.setProcessDefinitionId(processDefinitionId);
        suspendRequestDto.setIncludeProcessInstances(includeInstances);

        restHelper.putVoid(camunda, "/process-definition/key/" + processKey + "/suspended", suspendRequestDto);
    }

    public List<RuntimeUserTaskDto> findRuntimeUserTasks(HasRunningEngineData camunda, String processInstanceId) {
        return restHelper.getList(camunda, "/task?processInstanceId=" + processInstanceId, RuntimeUserTaskDto.class);
    }

    public List<RuntimeUserTaskDto> findRuntimeUserTasksByProcessKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/task?processDefinitionKey=" + processKey, RuntimeUserTaskDto.class);
    }

    public List<RuntimeIncidentDto> findRuntimeIncidentsByInstanceId(HasRunningEngineData camunda, String instanceId) {
        return restHelper.getList(camunda, "/incident?processInstanceId=" + instanceId, RuntimeIncidentDto.class);
    }

    public List<HistoricIncidentDto> findHistoricIncidentsByInstanceId(HasRunningEngineData camunda, String instanceId) {
        return restHelper.getList(camunda, "/history/incident?processInstanceId=" + instanceId, HistoricIncidentDto.class);
    }

    @Nullable
    public Boolean runtimeUserTaskExists(HasRunningEngineData camunda, String taskId) {
        try {
            restHelper.getOne(camunda, "/task/" + taskId, RuntimeUserTaskDto.class);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
        }
        return null;
    }

    public List<JobDto> getJobsByProcessKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/job?processDefinitionKey=" + processKey, JobDto.class);
    }

    public List<String> getJobIdsByProcessKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/job?processDefinitionKey=" + processKey, JobDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getJobIds(HasRunningEngineData camunda, JobListRequestDto request) {
        return restHelper.postList(camunda, "/job", request, JobDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<JobDto> getJobsByIds(HasRunningEngineData camunda, List<String> jobIds) {
        return restHelper.postList(camunda, "/job", Map.of("jobIds", jobIds), JobDto.class);
    }

    public List<JobDto> getFailedJobs(HasRunningEngineData camunda, List<String> processInstances) {
        Map<String, Object> body = Map.of("processInstanceIds", processInstances,
                "noRetriesLeft", true,
                "active", true);
        return restHelper.postList(camunda, "/job", body, JobDto.class);
    }

    @Nullable
    public JobDto getJobById(HasRunningEngineData camunda, String jobId) {
        try {
            return restHelper.getOne(camunda, "/job/" + jobId, JobDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
        }
        return null;
    }

    public boolean activeJobsExists(HasRunningEngineData camunda7, List<String> processInstances) {
        Map<String, Object> body = Map.of("processInstanceIds", processInstances,
                "withRetriesLeft", true,
                "active", true);
        CountResultDto countResultDto = restHelper.postOne(camunda7, "/job/count", body, CountResultDto.class);
        return countResultDto != null && countResultDto.getCount() > 0;
    }

    @Nullable
    public VariableInstanceDto getVariable(HasRunningEngineData camunda, String name) {
        try {
            List<VariableInstanceDto> variables = restHelper.getList(camunda, "/variable-instance?variableName=" + name, VariableInstanceDto.class);
            return CollectionUtils.isNotEmpty(variables) ? variables.get(0) : null;
        } catch (HttpClientErrorException e) {
            return null;
        }
    }

    @Nullable
    public HistoricUserTaskDto findHistoryUserTask(HasRunningEngineData camunda, String taskId) {
        List<HistoricUserTaskDto> tasks = restHelper.getList(camunda, "/history/task?taskId=" + taskId, HistoricUserTaskDto.class);

        return CollectionUtils.isNotEmpty(tasks) ? tasks.get(0) : null;
    }


    public RuntimeUserTaskDto findRuntimeUserTask(HasRunningEngineData camunda, String taskId) {
        return restHelper.getOne(camunda, "/task/" + taskId, RuntimeUserTaskDto.class);
    }

    public List<ProcessInstanceDto> findRuntimeProcessInstancesById(HasRunningEngineData camunda, String processId) {
        return restHelper.getList(camunda, "/process-instance?processDefinitionId=" + processId, ProcessInstanceDto.class);
    }

    public List<HistoricProcessInstanceDto> findHistoryProcessInstancesById(HasRunningEngineData camunda, String processId) {
        return restHelper.getList(camunda, "/history/process-instance?processDefinitionId=" + processId, HistoricProcessInstanceDto.class);
    }

    public List<ProcessInstanceDto> findRuntimeSubprocessInstances(HasRunningEngineData camunda, String parentProcessInstanceId) {
        return restHelper.getList(camunda, "/process-instance?superProcessInstanceId=" + parentProcessInstanceId, ProcessInstanceDto.class);
    }

    public List<HistoricDetailDto> getVariableLog(HasRunningEngineData camunda, String processInstanceId) {
        return restHelper.getList(camunda, "/history/detail?variableUpdates=true&excludeTaskDetails=true&processInstanceId=" + processInstanceId, HistoricDetailDto.class);
    }

    public ProcessDefinitionDto getProcessById(HasRunningEngineData camunda7, String id) {
        return restHelper.getOne(camunda7, "/process-definition/" + id, ProcessDefinitionDto.class);
    }

    public List<ProcessDefinitionDto> getProcessesByDeploymentId(HasRunningEngineData camunda7, String deploymentId) {
        return restHelper.getList(camunda7, "/process-definition?deploymentId=" + deploymentId, ProcessDefinitionDto.class);
    }

    @Nullable
    public Boolean existsProcessById(HasRunningEngineData camunda7, String id) {
        try {
            restHelper.getOne(camunda7, "/process-definition/" + id, ProcessDefinitionDto.class);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                return false;
            }
        }
        return null;
    }


    public String getBpmnXml(HasRunningEngineData camunda7, String definitionId) {
        return restHelper.getOne(camunda7, "/process-definition/" + definitionId + "/xml", BpmnXmlDto.class)
                .getBpmn20Xml();
    }


    @Nullable
    public DeploymentDto findDeployment(HasRunningEngineData camunda, String deploymentId) {
        try {
            return restHelper.getOne(camunda, "/deployment/" + deploymentId, DeploymentDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public List<ExternalTaskDto> getExternalTasks(HasRunningEngineData camunda, List<String> processInstanceIds) {
        return restHelper.postList(camunda, "/external-task", Map.of("processInstanceIdIn", processInstanceIds), ExternalTaskDto.class);
    }

    public List<ExternalTaskDto> getExternalTasksByIds(HasRunningEngineData camunda, List<String> externalTaskIds) {
        return restHelper.postList(camunda, "/external-task", Map.of("externalTaskIdIn", externalTaskIds), ExternalTaskDto.class);
    }

    public List<String> getExternalTaskIds(HasRunningEngineData camunda, List<String> processInstanceIds) {
        return restHelper.postList(camunda, "/external-task", Map.of("processInstanceIdIn", processInstanceIds), ExternalTaskDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public void failExternalTask(HasRunningEngineData camunda, String externalTaskId, HandleFailureDto handleFailureDto) {
        restHelper.postVoid(camunda, "/external-task/" + externalTaskId + "/lock", Map.of("workerId", handleFailureDto.getWorkerId(),
                "lockDuration", 10000));
        restHelper.postVoid(camunda, "/external-task/" + externalTaskId + "/failure", handleFailureDto);
        restHelper.postVoid(camunda, "/external-task/" + externalTaskId + "/unlock", Map.of());
    }

    public List<RuntimeUserTaskDto> getRuntimeUserTasks(HasRunningEngineData camunda) {
        return restHelper.getList(camunda, "/task", RuntimeUserTaskDto.class);
    }

    public List<String> getUserTasksByInstanceIds(HasRunningEngineData camunda, String processInstanceId) {
        return restHelper.getList(camunda, "/task?processInstanceId=" + processInstanceId, RuntimeUserTaskDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getRuntimeUserTasksByProcessKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/task?processDefinitionKey=" + processKey, IdDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getRuntimeUserTasks(HasRunningEngineData camunda, UserTaskListRequestDto request) {
        return restHelper.postList(camunda, "/task", request, IdDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public ExternalTaskDto getExternalTaskById(HasRunningEngineData camunda, String id) {
        return restHelper.getOne(camunda, "/external-task/" + id, ExternalTaskDto.class);
    }


    public ProcessVariablesMapDto getVariablesByProcess(HasRunningEngineData camunda7, String instanceId) {
        return restHelper.getOne(camunda7, "/process-instance/" + instanceId + "/variables", ProcessVariablesMapDto.class);
    }

    public List<RuntimeProcessInstanceDto> getRuntimeInstancesById(HasRunningEngineData camunda, String processId) {
        return restHelper.getList(camunda, "/process-instance?processDefinitionId=" + processId, RuntimeProcessInstanceDto.class);
    }

    @Nullable
    public RuntimeProcessInstanceDto getRuntimeInstanceById(HasRunningEngineData camunda, String instanceId) {
        try {
            return restHelper.getOne(camunda, "/process-instance/" + instanceId, RuntimeProcessInstanceDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    public HistoricProcessInstanceDto getHistoryInstanceById(HasRunningEngineData camunda, String instanceId) {
        try {
            return restHelper.getOne(camunda, "/history/process-instance/" + instanceId, HistoricProcessInstanceDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
        }
        return null;
    }

    public List<HistoricProcessInstanceDto> getHistoryInstancesById(HasRunningEngineData camunda, String processId) {
        return restHelper.getList(camunda, "/history/process-instance?processDefinitionId=" + processId, HistoricProcessInstanceDto.class);
    }

    public List<RuntimeProcessInstanceDto> getRuntimeInstancesByKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/process-instance?processDefinitionKey=" + processKey, RuntimeProcessInstanceDto.class);
    }

    public List<String> getSuspendedRuntimeInstancesByKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/process-instance?suspended=true&processDefinitionKey=" + processKey, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getSuspendedInstancesByProcessId(HasRunningEngineData camunda, String processId) {
        return restHelper.getList(camunda, "/process-instance?suspended=true&processDefinitionId=" + processId, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getActiveInstancesByProcessId(HasRunningEngineData camunda, String processId) {
        return restHelper.getList(camunda, "/process-instance?active=true&processDefinitionId=" + processId, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public boolean activeBatchExits(HasRunningEngineData camunda) {
        CountResultDto countResultDto = restHelper.getOne(camunda, "/batch/count", CountResultDto.class);
        return countResultDto != null && countResultDto.getCount() > 0;
    }

    public void waitForBatchExecution(HasRunningEngineData camunda) {
        boolean batchExists;
        int attempts = 0;
        do {
            batchExists = activeBatchExits(camunda);
            attempts++;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            if (attempts > 100) { //prevent infinite loop
                break;
            }
        } while (batchExists);
    }

    public List<String> getActiveRuntimeInstancesByKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/process-instance?active=true&processDefinitionKey=" + processKey, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getHistoricInstancesByKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/history/process-instance?processDefinitionKey=" + processKey, IdDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getSuspendedProcessesIdsByKey(HasRunningEngineData camunda, String key) {
        return restHelper.getList(camunda, "/process-definition?suspended=true&key=" + key, ProcessDefinitionDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getActiveProcessesIdsByKey(HasRunningEngineData camunda, String key) {
        return restHelper.getList(camunda, "/process-definition?active=true&key=" + key, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public void terminateExternallyInstance(HasRunningEngineData camunda, String instanceId) {
        restHelper.delete(camunda, "/process-instance/" + instanceId);
    }

    public void completeTaskById(HasRunningEngineData camunda, String taskId) {
        restHelper.postOne(camunda, "/task/" + taskId + "/complete", new CompleteUserTaskDto(), ProcessVariablesMapDto.class);
    }

    public RuntimeUserTaskDto createUserTask(HasRunningEngineData camunda, String name, String assignee) {
        UUID taskId = UUID.randomUUID();
        Map<String, Object> body = Map.of(
                "id", taskId,
                "name", name,
                "removalTime", OffsetDateTime.now().plusMinutes(1),
                "assignee", assignee);
        restHelper.postVoid(camunda, "/task/create", body);
        return restHelper.getOne(camunda, "/task/" + taskId, RuntimeUserTaskDto.class);
    }

    public void setJobRetries(HasRunningEngineData camunda, String jobId, int retries) {
        restHelper.putVoid(camunda, "/job/" + jobId + "/retries", new SetJobRetriesDto(retries));
    }

    public List<String> getIncidentIdsByProcessKey(HasRunningEngineData camunda, String processKey) {
        return restHelper.getList(camunda, "/incident?processDefinitionKeyIn=" + processKey, IdDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getIncidentIdsByInstanceId(HasRunningEngineData camunda, String processInstanceId) {
        return restHelper.getList(camunda, "/incident?processInstanceId=" + processInstanceId, IncidentDto.class)
                .stream()
                .map(IncidentDto::getId)
                .toList();
    }
}
