/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.flowset.control.entity.deployment.DeploymentResourceType;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of the content validation of a single deployment resource.
 */
@Getter
public class DeploymentResourceValidationResult {

    /**
     * Resource type determined by the file name, {@code null} if the type is not supported.
     */
    @Nullable
    protected final DeploymentResourceType type;

    /**
     * Keys of the definitions contained in the resource: process ids (BPMN), decision ids (DMN) or a form id.
     */
    protected final List<String> keys = new ArrayList<>();

    /**
     * Localized error messages. A resource with errors must not be deployed.
     */
    protected final List<String> errors = new ArrayList<>();

    /**
     * Localized warning messages. Warnings do not block the deployment.
     */
    protected final List<String> warnings = new ArrayList<>();

    /**
     * References from the resource to other resources: called processes, decisions, forms, scripts (BPMN only).
     */
    protected final List<ResourceReference> references = new ArrayList<>();

    public DeploymentResourceValidationResult(@Nullable DeploymentResourceType type) {
        this.type = type;
    }

    public DeploymentResourceStatus getStatus() {
        if (!errors.isEmpty()) {
            return DeploymentResourceStatus.ERROR;
        }
        if (!warnings.isEmpty()) {
            return DeploymentResourceStatus.WARNING;
        }
        return DeploymentResourceStatus.OK;
    }

    public void addKey(String key) {
        keys.add(key);
    }

    public void addError(String error) {
        errors.add(error);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public void addReference(ResourceReference reference) {
        references.add(reference);
    }
}
