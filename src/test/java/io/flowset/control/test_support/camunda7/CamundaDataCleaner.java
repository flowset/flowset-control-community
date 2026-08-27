/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.entity.engine.EngineType;
import io.flowset.control.test_support.EngineDataCleaner;
import io.flowset.control.test_support.EngineTestContainerRestHelper;
import io.flowset.control.test_support.camunda7.dto.IdDto;
import io.flowset.control.test_support.camunda7.dto.response.CountResultDto;
import io.flowset.control.test_support.engine.HasRunningEngineData;
import io.flowset.control.test_support.engine.external.ExternalEngine;
import io.flowset.control.test_support.testcontainers.EngineContainer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

@Slf4j
@Component("control_CamundaDataCleaner")
public class CamundaDataCleaner implements EngineDataCleaner<HasRunningEngineData> {
    private final List<String> runtimeResources = ImmutableList.of("/deployment", "/batch",
            "/process-instance", "/job",
            "/task", "/incident", "/external-task",
            "/variable-instance"
    );

    private final List<String> historicResources = ImmutableList.of("/history/process-instance", "/history/batch",
            "/history/job-log", "/history/task", "/history/incident",
            "/history/external-task-log", "/history/variable-instance", "/history/detail"
    );

    private final EngineTestContainerRestHelper restHelper;
    private final CamundaRestTestHelper camundaRestTestHelper;


    public CamundaDataCleaner(EngineTestContainerRestHelper restHelper, CamundaRestTestHelper camundaRestTestHelper) {
        this.restHelper = restHelper;
        this.camundaRestTestHelper = camundaRestTestHelper;
    }

    public void clean(HasRunningEngineData camunda) {
        if (isEngineRunning(camunda)) {
            // remove decision instances
            removeDecisionInstancesAsync(camunda);

            //remove runtime data
            removeResourceByIds(camunda, "/job");
            removeResourceByIds(camunda, "/batch");
            removeResourceByIds(camunda, "/process-instance", "skipCustomListeners=true");
            removeResourceByIds(camunda, "/process-definition", "skipCustomListeners=true");
            removeResourceByIds(camunda, "/deployment",  "skipCustomListeners=true");
            removeResourceByIds(camunda, "/task");

            //remove history data
            removeResourceByIds(camunda, "/history/batch");
            removeResourceByIds(camunda, "/history/process-instance", "failIfNotExists=false");

            logDataCleanResult(camunda);
        }
    }

    protected boolean isEngineRunning(HasRunningEngineData camunda) {
        if (camunda instanceof EngineContainer<?> engineContainer) {
            return engineContainer.isRunning();
        }

        if (camunda instanceof ExternalEngine externalEngine) {
            try {
                restHelper.getOne(externalEngine, "/version", Map.class);
                return true;
            } catch (Exception e) {
                log.error("Unable to get external Camunda engine version", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean supports(HasRunningEngineData engineContainer) {
        EngineType engineType = engineContainer.getEngineType();
        return engineType == EngineType.CAMUNDA_7 || engineType == EngineType.OPERATON;
    }

    private void logDataCleanResult(HasRunningEngineData camunda) {
        try {
            StringBuilder notEmptyRuntimeResourcesLog = new StringBuilder();
            checkResourceCount(camunda, runtimeResources, notEmptyRuntimeResourcesLog);
            if (!notEmptyRuntimeResourcesLog.isEmpty()) {
                log.warn(notEmptyRuntimeResourcesLog.toString());
            }

            StringBuilder notEmptyHistoricResourcesLog = new StringBuilder();
            checkResourceCount(camunda, historicResources, notEmptyHistoricResourcesLog);
            if (!notEmptyHistoricResourcesLog.isEmpty()) {
                log.warn(notEmptyHistoricResourcesLog.toString());
            }
        } catch (Exception e) {
            log.error("Unable to get Camunda engine container clean result", e);
        }
    }

    private void checkResourceCount(HasRunningEngineData camunda, List<String> resources, StringBuilder logMessage) {
        resources.forEach(resource -> {
            String resourcePath = resource + "/count";
            long count = getCount(camunda, resourcePath);
            if (count > 0) {
                logMessage.append("Found non-zero count by resource")
                        .append(resourcePath)
                        .append(": ")
                        .append(count)
                        .append("\n");
            }
        });
    }

    private long getCount(HasRunningEngineData engineContainer, String resourcePath) {
        return restHelper.getOne(engineContainer, resourcePath, CountResultDto.class).getCount();
    }

    private void removeResourceByIds(HasRunningEngineData engineContainer, String resourcePath, String...queryParams) {
        try {

            List<IdDto> idDtoList = restHelper.getList(engineContainer, resourcePath, IdDto.class);

            int count = 0;
            String queryParamsString;
            for (IdDto idDto : idDtoList) {
                queryParamsString = queryParams != null && queryParams.length > 0 ?
                        "?" + String.join("&", queryParams) : "";
                restHelper.delete(engineContainer, resourcePath + "/" + idDto.getId() + queryParamsString);
                count++;
            }
            log.info("Remove by resource {}: {} items", resourcePath, count);
        } catch (Exception e) {
            log.error("Unable to remove Camunda resources by path {}", resourcePath, e);
        }
    }

    protected void removeDecisionInstancesAsync(HasRunningEngineData engineContainer) {
        try {
            List<String> ids = restHelper.getList(engineContainer, "/history/decision-instance", IdDto.class)
                    .stream()
                    .map(IdDto::getId)
                    .toList();

            if (CollectionUtils.isNotEmpty(ids)) {
                restHelper.postVoid(engineContainer, "/history/decision-instance/delete", Map.of(
                        "historicDecisionInstanceIds", ids));

                camundaRestTestHelper.waitForBatchExecution(engineContainer);
                log.info("Remove by decision instances: {} items", ids.size());
            }

        } catch (Exception e) {
            log.error("Unable to remove Camunda historic decision instances", e);
        }
    }

}
