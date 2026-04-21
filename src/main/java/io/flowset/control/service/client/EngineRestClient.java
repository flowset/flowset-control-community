package io.flowset.control.service.client;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.exception.EngineConnectionFailedException;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.engine.auth.EngineAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collections;

@Component("control_EngineRestClient")
public class EngineRestClient {

    protected static final Logger log = LoggerFactory.getLogger(EngineRestClient.class);

    protected final RestTemplate restTemplate;
    protected final EngineService engineService;
    protected final EngineAuthenticator engineAuthenticator;

    public EngineRestClient(EngineService engineService,
                            EngineAuthenticator engineAuthenticator) {
        this.engineService = engineService;
        this.engineAuthenticator = engineAuthenticator;

        this.restTemplate = new RestTemplate();
    }

    public void updateVariableBinary(VariableInstanceData variableInstanceData, File data) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("data", new FileSystemResource(data));
        body.add("valueType", variableInstanceData.getType());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        BpmEngine engine = engineService.getSelectedEngine();
        if (engine == null) {
            throw new EngineConnectionFailedException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Server unavailable");
        }

        engineAuthenticator.applyAuthentication(engine, headers);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String name = variableInstanceData.getName();
        String url = engine.getBaseUrl() + "/process-instance/" + variableInstanceData.getExecutionId() + "/variables/" + name + "/data";

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Error on update process variable, process id {}, process name {}, status code {}",
                    variableInstanceData.getVariableInstanceId(), name, response.getStatusCode());
        }
    }

    public ResponseEntity<String> getStacktrace(String jobId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        BpmEngine engine = engineService.getSelectedEngine();
        if (engine == null) {
            throw new EngineConnectionFailedException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Server unavailable");
        }
        engineAuthenticator.applyAuthentication(engine, headers);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                engine.getBaseUrl() + "/job/" + jobId + "/stacktrace",
                HttpMethod.GET,
                entity,
                String.class
        );
    }
}
