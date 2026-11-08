/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

/**
 * A reference from a deployment resource with the location where the referenced resource is found.
 *
 * @param fileName  name of the file that contains the reference
 * @param reference the reference
 * @param location  where the referenced resource is found
 * @param severity  {@link DeploymentResourceStatus#ERROR} blocks the deployment
 * @param message   localized explanation, empty for a resolved reference
 */
public record ResolvedReference(String fileName,
                                ResourceReference reference,
                                ReferenceLocation location,
                                DeploymentResourceStatus severity,
                                String message) {
}
