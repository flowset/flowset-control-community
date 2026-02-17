/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.test_support.EngineTestContainerRestHelper;
import io.flowset.control.test_support.camunda7.dto.IdDto;
import io.flowset.control.test_support.camunda7.dto.request.CompleteUserTaskDto;
import io.flowset.control.test_support.camunda7.dto.request.HandleFailureDto;
import io.flowset.control.test_support.camunda7.dto.request.SetJobRetriesDto;
import io.flowset.control.test_support.camunda7.dto.request.StartProcessDto;
import io.flowset.control.test_support.camunda7.dto.request.SuspendInstancesRequestDto;
import io.flowset.control.test_support.camunda7.dto.request.SuspendProcessRequestDto;
import io.flowset.control.test_support.camunda7.dto.request.SuspendRequestDto;
import io.flowset.control.test_support.camunda7.dto.response.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component("control_CamundaRestTestHelper")
public class CamundaRestTestHelper {
    private final EngineTestContainerRestHelper restHelper;

    public CamundaRestTestHelper(EngineTestContainerRestHelper restHelper) {
        this.restHelper = restHelper;
    }

    public DeploymentResultDto createDeployment(Camunda7Container<?> camunda, String resourceClassPath) {
        String name = FilenameUtils.getName(resourceClassPath);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("deployment-name", name);
        parts.add(name, new ClassPathResource(resourceClassPath));

        return restHelper.postOne(camunda, "/deployment/create", parts, DeploymentResultDto.class);
    }

    public DeploymentResultDto createDeployment(Camunda7Container<?> camunda, String resourceName, String resourceContent) {
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

    public RuntimeProcessInstanceDto findRuntimeInstance(Camunda7Container<?> camunda, String processInstanceId) {
        return restHelper.getOne(camunda, "/process-instance/" + processInstanceId, RuntimeProcessInstanceDto.class);
    }

    public long getRunningProcessesCount(Camunda7Container<?> camunda) {
        CountResultDto body = restHelper.getOne(camunda, "/process-instance/count", CountResultDto.class);

        return body != null ? body.getCount() : -1;
    }

    public RuntimeProcessInstanceDto startProcessByKey(Camunda7Container<?> camunda, String processKey, StartProcessDto dto) {
        return restHelper.postOne(camunda, "/process-definition/key/" + processKey + "/start", dto, RuntimeProcessInstanceDto.class);
    }

    public void suspendInstanceById(Camunda7Container<?> camunda, String instanceId) {
        SuspendRequestDto suspendRequestDto = new SuspendRequestDto();
        suspendRequestDto.setSuspended(true);

        restHelper.putVoid(camunda, "/process-instance/" + instanceId + "/suspended", suspendRequestDto);
    }

    public void suspendJobById(Camunda7Container<?> camunda, String jobId) {
        SuspendRequestDto suspendRequestDto = new SuspendRequestDto();
        suspendRequestDto.setSuspended(true);

        restHelper.putVoid(camunda, "/job/" + jobId + "/suspended", suspendRequestDto);
    }

    public void suspendInstanceByProcessId(Camunda7Container<?> camunda, String processId) {
        SuspendInstancesRequestDto suspendRequestDto = new SuspendInstancesRequestDto();
        suspendRequestDto.setSuspended(true);
        suspendRequestDto.setProcessDefinitionId(processId);

        restHelper.putVoid(camunda, "/process-instance/suspended", suspendRequestDto);
    }

    public void suspendProcessByKey(Camunda7Container<?> camunda, String processKey, boolean includeInstances) {
        SuspendProcessRequestDto suspendRequestDto = new SuspendProcessRequestDto();
        suspendRequestDto.setSuspended(true);
        suspendRequestDto.setIncludeProcessInstances(includeInstances);

        restHelper.putVoid(camunda, "/process-definition/key/" + processKey + "/suspended", suspendRequestDto);
    }

    public void suspendProcessById(Camunda7Container<?> camunda, String processKey,
                                   String processDefinitionId, boolean includeInstances) {
        SuspendProcessRequestDto suspendRequestDto = new SuspendProcessRequestDto();
        suspendRequestDto.setSuspended(true);
        suspendRequestDto.setProcessDefinitionId(processDefinitionId);
        suspendRequestDto.setIncludeProcessInstances(includeInstances);

        restHelper.putVoid(camunda, "/process-definition/key/" + processKey + "/suspended", suspendRequestDto);
    }

    public List<RuntimeUserTaskDto> findRuntimeUserTasks(Camunda7Container<?> camunda, String processInstanceId) {
        return restHelper.getList(camunda, "/task?processInstanceId=" + processInstanceId, RuntimeUserTaskDto.class);
    }

    public List<RuntimeUserTaskDto> findRuntimeUserTasksByProcessKey(Camunda7Container<?> camunda, String processKey) {
        return restHelper.getList(camunda, "/task?processDefinitionKey=" + processKey, RuntimeUserTaskDto.class);
    }

    public List<RuntimeIncidentDto> findRuntimeIncidentsByInstanceId(Camunda7Container<?> camunda, String instanceId) {
        return restHelper.getList(camunda, "/incident?processInstanceId=" + instanceId, RuntimeIncidentDto.class);
    }

    public List<HistoricIncidentDto> findHistoricIncidentsByInstanceId(Camunda7Container<?> camunda, String instanceId) {
        return restHelper.getList(camunda, "/history/incident?processInstanceId=" + instanceId, HistoricIncidentDto.class);
    }

    @Nullable
    public Boolean runtimeUserTaskExists(Camunda7Container<?> camunda, String taskId) {
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

    public List<JobDto> getJobsByProcessKey(Camunda7Container<?> camunda, String processKey) {
        return restHelper.getList(camunda, "/job?processDefinitionKey=" + processKey, JobDto.class);
    }

    public List<String> getJobIdsByProcessKey(Camunda7Container<?> camunda, String processKey) {
        return restHelper.getList(camunda, "/job?processDefinitionKey=" + processKey, JobDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<JobDto> getJobsByIds(Camunda7Container<?> camunda, List<String> jobIds) {
        return restHelper.postList(camunda, "/job", Map.of("jobIds", jobIds), JobDto.class);
    }

    public List<JobDto> getFailedJobs(Camunda7Container<?> camunda, List<String> processInstances) {
        Map<String, Object> body = Map.of("processInstanceIds", processInstances,
                "noRetriesLeft", true,
                "active", true);
        return restHelper.postList(camunda, "/job", body, JobDto.class);
    }

    @Nullable
    public JobDto getJobById(Camunda7Container<?> camunda, String jobId) {
        try {
            return restHelper.getOne(camunda, "/job/" + jobId, JobDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
        }
        return null;
    }

    public boolean activeJobsExists(Camunda7Container<?> camunda7, List<String> processInstances) {
        Map<String, Object> body = Map.of("processInstanceIds", processInstances,
                "withRetriesLeft", true,
                "active", true);
        CountResultDto countResultDto = restHelper.postOne(camunda7, "/job/count", body, CountResultDto.class);
        return countResultDto != null && countResultDto.getCount() > 0;
    }

    @Nullable
    public VariableInstanceDto getVariable(Camunda7Container<?> camunda, String name) {
        try {
            List<VariableInstanceDto> variables = restHelper.getList(camunda, "/variable-instance?variableName=" + name, VariableInstanceDto.class);
            return CollectionUtils.isNotEmpty(variables) ? variables.get(0) : null;
        } catch (HttpClientErrorException e) {
            return null;
        }
    }

    @Nullable
    public HistoricUserTaskDto findHistoryUserTask(Camunda7Container<?> camunda, String taskId) {
        List<HistoricUserTaskDto> tasks = restHelper.getList(camunda, "/history/task?taskId=" + taskId, HistoricUserTaskDto.class);

        return CollectionUtils.isNotEmpty(tasks) ? tasks.get(0) : null;
    }


    public RuntimeUserTaskDto findRuntimeUserTask(Camunda7Container<?> camunda, String taskId) {
        return restHelper.getOne(camunda, "/task/" + taskId, RuntimeUserTaskDto.class);
    }

    @Nullable
    public List<ProcessInstanceDto> findRuntimeProcessInstancesById(Camunda7Container<?> camunda, String processId) {
        return restHelper.getList(camunda, "/process-instance?processDefinitionId=" + processId, ProcessInstanceDto.class);
    }

    @Nullable
    public List<HistoricProcessInstanceDto> findHistoryProcessInstancesById(Camunda7Container<?> camunda, String processId) {
        return restHelper.getList(camunda, "/history/process-instance?processDefinitionId=" + processId, HistoricProcessInstanceDto.class);
    }

    @Nullable
    public List<ProcessInstanceDto> findRuntimeSubprocessInstances(Camunda7Container<?> camunda, String parentProcessInstanceId) {
        return restHelper.getList(camunda, "/process-instance?superProcessInstanceId=" + parentProcessInstanceId, ProcessInstanceDto.class);
    }

    public List<HistoricDetailDto> getVariableLog(Camunda7Container<?> camunda, String processInstanceId) {
        return restHelper.getList(camunda, "/history/detail?variableUpdates=true&excludeTaskDetails=true&processInstanceId=" + processInstanceId, HistoricDetailDto.class);
    }

    public ProcessDefinitionDto getProcessById(Camunda7Container<?> camunda7, String id) {
        return restHelper.getOne(camunda7, "/process-definition/" + id, ProcessDefinitionDto.class);
    }

    public List<ProcessDefinitionDto> getProcessesByDeploymentId(Camunda7Container<?> camunda7, String deploymentId) {
        return restHelper.getList(camunda7, "/process-definition?deploymentId=" + deploymentId, ProcessDefinitionDto.class);
    }

    @Nullable
    public Boolean existsProcessById(Camunda7Container<?> camunda7, String id) {
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


    public String getBpmnXml(Camunda7Container<?> camunda7, String definitionId) {
        return restHelper.getOne(camunda7, "/process-definition/" + definitionId + "/xml", BpmnXmlDto.class)
                .getBpmn20Xml();
    }


    @Nullable
    public DeploymentDto findDeployment(Camunda7Container<?> camunda, String deploymentId) {
        try {
            return restHelper.getOne(camunda, "/deployment/" + deploymentId, DeploymentDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public List<ExternalTaskDto> getExternalTasks(Camunda7Container<?> camunda, List<String> processInstanceIds) {
        return restHelper.postList(camunda, "/external-task", Map.of("processInstanceIdIn", processInstanceIds), ExternalTaskDto.class);
    }

    public List<ExternalTaskDto> getExternalTasksByIds(Camunda7Container<?> camunda, List<String> externalTaskIds) {
        return restHelper.postList(camunda, "/external-task", Map.of("externalTaskIdIn", externalTaskIds), ExternalTaskDto.class);
    }

    public List<String> getExternalTaskIds(Camunda7Container<?> camunda, List<String> processInstanceIds) {
        return restHelper.postList(camunda, "/external-task", Map.of("processInstanceIdIn", processInstanceIds), ExternalTaskDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public void failExternalTask(Camunda7Container<?> camunda, String externalTaskId, HandleFailureDto handleFailureDto) {
        restHelper.postVoid(camunda, "/external-task/" + externalTaskId + "/lock", Map.of("workerId", handleFailureDto.getWorkerId(),
                "lockDuration", 10000));
        restHelper.postVoid(camunda, "/external-task/" + externalTaskId + "/failure", handleFailureDto);
        restHelper.postVoid(camunda, "/external-task/" + externalTaskId + "/unlock", Map.of());
    }

    public List<RuntimeUserTaskDto> getRuntimeUserTasks(Camunda7Container<?> camunda) {
        return restHelper.getList(camunda, "/task", RuntimeUserTaskDto.class);
    }

    public ExternalTaskDto getExternalTaskById(Camunda7Container<?> camunda, String id) {
        return restHelper.getOne(camunda, "/external-task/" + id, ExternalTaskDto.class);
    }


    public ProcessVariablesMapDto getVariablesByProcess(Camunda7Container<?> camunda7, String instanceId) {
        return restHelper.getOne(camunda7, "/process-instance/" + instanceId + "/variables", ProcessVariablesMapDto.class);
    }

    public List<RuntimeProcessInstanceDto> getRuntimeInstancesById(Camunda7Container<?> camunda, String processId) {
        return restHelper.getList(camunda, "/process-instance?processDefinitionId=" + processId, RuntimeProcessInstanceDto.class);
    }

    @Nullable
    public RuntimeProcessInstanceDto getRuntimeInstanceById(Camunda7Container<?> camunda, String instanceId) {
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
    public HistoricProcessInstanceDto getHistoryInstanceById(Camunda7Container<?> camunda, String instanceId) {
        try {
            return restHelper.getOne(camunda, "/history/process-instance/" + instanceId, HistoricProcessInstanceDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
        }
        return null;
    }

    public List<HistoricProcessInstanceDto> getHistoryInstancesById(Camunda7Container<?> camunda, String processId) {
        return restHelper.getList(camunda, "/history/process-instance?processDefinitionId=" + processId, HistoricProcessInstanceDto.class);
    }

    public List<RuntimeProcessInstanceDto> getRuntimeInstancesByKey(Camunda7Container<?> camunda, String processKey) {
        return restHelper.getList(camunda, "/process-instance?processDefinitionKey=" + processKey, RuntimeProcessInstanceDto.class);
    }

    public List<String> getSuspendedRuntimeInstancesByKey(Camunda7Container<?> camunda, String processKey) {
        return restHelper.getList(camunda, "/process-instance?suspended=true&processDefinitionKey=" + processKey, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getSuspendedInstancesByProcessId(Camunda7Container<?> camunda, String processId) {
        return restHelper.getList(camunda, "/process-instance?suspended=true&processDefinitionId=" + processId, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getActiveInstancesByProcessId(Camunda7Container<?> camunda, String processId) {
        return restHelper.getList(camunda, "/process-instance?active=true&processDefinitionId=" + processId, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public boolean activeBatchExits(Camunda7Container<?> camunda) {
        CountResultDto countResultDto = restHelper.getOne(camunda, "/batch/count", CountResultDto.class);
        return countResultDto != null && countResultDto.getCount() > 0;
    }

    public List<String> getActiveRuntimeInstancesByKey(Camunda7Container<?> camunda, String processKey) {
        return restHelper.getList(camunda, "/process-instance?active=true&processDefinitionKey=" + processKey, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getHistoricInstancesByKey(Camunda7Container<?> camunda, String processKey) {
        return restHelper.getList(camunda, "/history/process-instance?processDefinitionKey=" + processKey, IdDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getSuspendedProcessesIdsByKey(Camunda7Container<?> camunda, String key) {
        return restHelper.getList(camunda, "/process-definition?suspended=true&key=" + key, ProcessDefinitionDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public List<String> getActiveProcessesIdsByKey(Camunda7Container<?> camunda, String key) {
        return restHelper.getList(camunda, "/process-definition?active=true&key=" + key, RuntimeProcessInstanceDto.class)
                .stream()
                .map(IdDto::getId)
                .toList();
    }

    public void terminateExternallyInstance(Camunda7Container<?> camunda, String instanceId) {
        restHelper.delete(camunda, "/process-instance/" + instanceId);
    }

    public void completeTaskById(Camunda7Container<?> camunda, String taskId) {
        restHelper.postOne(camunda, "/task/" + taskId + "/complete", new CompleteUserTaskDto(), ProcessVariablesMapDto.class);
    }

    public void setJobRetries(Camunda7Container<?> camunda, String jobId, int retries) {
        restHelper.putVoid(camunda, "/job/" + jobId + "/retries", new SetJobRetriesDto(retries));
    }
}
