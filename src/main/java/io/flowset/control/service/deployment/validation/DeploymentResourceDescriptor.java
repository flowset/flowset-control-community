/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.flowset.control.entity.deployment.DeploymentResourceType;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A resource of a deployment being prepared, as an input of the {@link DeploymentReferenceChecker}.
 *
 * @param fileName   resource file name
 * @param type       resource type, {@code null} if not supported
 * @param keys       definition keys of the resource: process ids (BPMN), decision ids (DMN) or a form id
 * @param references references from the resource to other resources (BPMN only)
 */
public record DeploymentResourceDescriptor(String fileName,
                                           @Nullable DeploymentResourceType type,
                                           List<String> keys,
                                           List<ResourceReference> references) {

    public DeploymentResourceDescriptor {
        keys = List.copyOf(keys);
        references = List.copyOf(references);
    }

    /**
     * Creates a descriptor from the content validation result of a resource.
     */
    public static DeploymentResourceDescriptor of(String fileName, DeploymentResourceValidationResult result) {
        return new DeploymentResourceDescriptor(fileName, result.getType(), result.getKeys(), result.getReferences());
    }
}
