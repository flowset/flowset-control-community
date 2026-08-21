/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.restsupport.camunda;

import feign.Response;
import feign.codec.ErrorDecoder;
import io.flowset.control.exception.RemoteEngineParseException;
import io.flowset.control.exception.RemoteProcessEngineException;
import org.apache.commons.io.IOUtils;
import org.camunda.community.rest.config.CamundaRestClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles response errors from the Camunda REST API.
 */
public class CamundaFeignErrorDecoder implements ErrorDecoder {
    protected static final Logger log = LoggerFactory.getLogger(CamundaFeignErrorDecoder.class);

    protected final CamundaRestClientProperties camundaRestClientProperties;
    protected final ErrorDecoder defaultErrorDecoder;
    protected final JsonMapper objectMapper;

    public CamundaFeignErrorDecoder(CamundaRestClientProperties camundaRestClientProperties) {
        this.camundaRestClientProperties = camundaRestClientProperties;
        this.objectMapper = new JsonMapper();
        this.defaultErrorDecoder = new Default();
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        List<Integer> httpCodes = camundaRestClientProperties.getErrorDecoding().getHttpCodes();
        if (httpCodes.contains(response.status())) {
            Exception decodedException = decodeException(response);
            if (decodedException != null) {
                boolean wrapExceptions = camundaRestClientProperties.getErrorDecoding().getWrapExceptions();
                if (wrapExceptions && !(decodedException instanceof RemoteProcessEngineException)) {
                    return new RemoteProcessEngineException("Error during remote BPM engine engine invocation", decodedException);
                } else {
                    return decodedException;
                }
            } else {
                return new RemoteProcessEngineException("Error during remote BPM engine invocation of %s: %s".formatted(methodKey, response.reason()));
            }
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }

    /**
     * Creates an exception depending on the provided response.
     *
     * @param feignResponse response received from the Camunda
     * @return exception instance
     */
    protected Exception decodeException(Response feignResponse) {
        try {
            String responseContent = IOUtils.toString(feignResponse.body().asInputStream(), StandardCharsets.UTF_8);
            CamundaErrorResponse errorResponse = objectMapper.readValue(responseContent, CamundaErrorResponse.class);

            String type = errorResponse.getType();

            Exception exceptionFromResponse;
            if (type.equals("ParseException")) {
                exceptionFromResponse = createParseException(responseContent);
            } else {
                exceptionFromResponse = createExceptionByType(errorResponse);
            }

            return exceptionFromResponse != null ? exceptionFromResponse : RemoteProcessEngineException.defaultException(errorResponse);
        } catch (IOException e) {
            log.error("Unable to parse error response from BPM engine: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates an exception depending on the type from the provided response.
     *
     * @param errorResponse Camunda error response
     * @return created exception or null if creation fails
     */
    protected Exception createExceptionByType(CamundaErrorResponse errorResponse) {
        try {
            Class<?> exceptionClass = Class.forName(errorResponse.getType());
            if (Throwable.class.isAssignableFrom(exceptionClass)) {
                Constructor<?> constructor = exceptionClass.getConstructor(String.class);

                return (Exception) constructor.newInstance(errorResponse.getMessage());
            } else {
                return null;
            }
        } catch (Exception e) {
            log.debug("Unable to create exception by returned type {}: {}", errorResponse.getType(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates an instance of {@link RemoteEngineParseException} with the list of error from the provided response.
     *
     * @param responseContent Camunda response JSON
     * @return {@link RemoteEngineParseException} instance or null if error occurred during JSON processing
     */
    @Nullable
    protected Exception createParseException(String responseContent) {
        try {
            ParseExceptionResponse parseExceptionResponse = objectMapper.readValue(responseContent, ParseExceptionResponse.class);
            return new RemoteEngineParseException(parseExceptionResponse.getMessage(), parseExceptionResponse.getDetails());
        } catch (JacksonException e) {
            log.error("Unable to parse exception response from BPM engine: {}", e.getMessage(), e);
            return null;
        }
    }
}
