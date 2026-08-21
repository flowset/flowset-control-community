/*
 * Copyright (c) Haulmont 2025. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.exception;

import io.flowset.control.restsupport.camunda.CamundaErrorResponse;
import org.apache.commons.lang3.Strings;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * Generic class for exceptions thrown by remote BPM engine.
 */
public class RemoteProcessEngineException extends ProcessEngineException {
    protected String responseMessage;
    protected String engineExceptionType;

    public RemoteProcessEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteProcessEngineException(String message) {
        super(message);
    }

    public String getEngineExceptionMessage() {
        if (engineExceptionType == null) {
            return responseMessage;
        }
        return engineExceptionType + ": " + responseMessage;
    }

    public static RemoteProcessEngineException defaultException(CamundaErrorResponse response) {
        return new RemoteProcessEngineException("Error during remote BPM engine invocation with %s: %s".formatted(response.getType(), response.getMessage()))
                .withResponseMessage(response.getMessage())
                .withEngineExceptionType(response.getType());
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getEngineExceptionType() {
        return engineExceptionType;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public void setEngineExceptionType(String engineExceptionType) {
        this.engineExceptionType = engineExceptionType;
    }

    public RemoteProcessEngineException withResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
        return this;
    }

    public RemoteProcessEngineException withEngineExceptionType(String engineExceptionType) {
        this.engineExceptionType = engineExceptionType;
        return this;
    }

    public boolean isProcessEngineException() {
        return Strings.CS.equals("ProcessEngineException", engineExceptionType);
    }
}
