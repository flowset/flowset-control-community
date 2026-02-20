/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.test_support.camunda7;

import io.flowset.control.test_support.EngineDataCleaner;
import io.flowset.control.test_support.EngineTestContainerRestHelper;
import io.flowset.control.test_support.camunda7.dto.IdDto;
import io.flowset.control.test_support.camunda7.dto.response.CountResultDto;
import io.flowset.control.test_support.testcontainers.EngineContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.List;

@Slf4j
@Component("control_CamundaDataCleaner")
public class CamundaDataCleaner implements EngineDataCleaner<Camunda7Container<?>> {
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


    public CamundaDataCleaner(EngineTestContainerRestHelper restHelper) {
        this.restHelper = restHelper;
    }

    public void clean(Camunda7Container<?> camunda) {
        if (camunda.isRunning()) {
            //remove runtime data
            removeResourceByIds(camunda, "/batch");
            removeResourceByIds(camunda, "/job");
            removeResourceByIds(camunda, "/process-instance");
            removeResourceByIds(camunda, "/process-definition");
            removeResourceByIds(camunda, "/decision-definition");
            removeResourceByIds(camunda, "/deployment");

            //remove history data
            removeResourceByIds(camunda, "/history/batch");
            removeResourceByIds(camunda, "/history/process-instance");
            removeResourceByIds(camunda, "/history/decision-instance");

            logDataCleanResult(camunda);
        }
    }

    @Override
    public boolean supports(EngineContainer<?> engineContainer) {
        return engineContainer instanceof Camunda7Container<?>;
    }

    private void logDataCleanResult(Camunda7Container<?> camunda) {
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

    private void checkResourceCount(Camunda7Container<?> camunda, List<String> resources, StringBuilder logMessage) {
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

    private long getCount(Camunda7Container<?> camunda, String resourcePath) {
        return restHelper.getOne(camunda, resourcePath, CountResultDto.class).getCount();
    }

    private void removeResourceByIds(Camunda7Container<?> engineContainer, String resourcePath) {
        try {
            List<IdDto> idDtoList = restHelper.getList(engineContainer, resourcePath, IdDto.class);

            int count = 0;
            for (IdDto idDto : idDtoList) {
                restHelper.delete(engineContainer, resourcePath + "/" + idDto.getId());
                count++;
            }
            log.info("Remove by resource {}: {} items", resourcePath, count);
        } catch (Exception e) {
            log.error("Unable to remove Camunda resources by path {}", resourcePath, e);
        }
    }

}
