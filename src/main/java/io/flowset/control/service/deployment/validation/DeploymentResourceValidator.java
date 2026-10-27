/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

/**
 * Validates the content of resources uploaded to a deployment before the deployment is created.
 */
public interface DeploymentResourceValidator {

    /**
     * Validates a deployment resource: checks that the file type is supported, the content is not empty
     * and matches the type (well-formed BPMN/DMN XML, Camunda Form JSON, text for scripts and HTML forms,
     * a real image for images). Collects the definition keys (process, decision or form ids) and, for BPMN,
     * the references to other resources (called processes, decisions, forms, scripts).
     * <p>
     * The method never throws: unexpected failures are reported as errors in the result.
     *
     * @param fileName resource file name
     * @param content  resource content
     * @return validation result
     */
    DeploymentResourceValidationResult validate(String fileName, byte[] content);
}
