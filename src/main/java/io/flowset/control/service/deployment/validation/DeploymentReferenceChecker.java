/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import java.util.List;

/**
 * Checks the references between the resources of a deployment being prepared and the definitions already deployed
 * to the selected engine.
 */
public interface DeploymentReferenceChecker {

    /**
     * Resolves the references of the BPMN resources (called processes, decisions, forms, scripts): in the same
     * deployment, in the engine or missing. A missing resource with the deployment binding is an error, with other
     * bindings a warning. Also reports duplicate process, decision and form keys within the deployment as errors.
     * <p>
     * The method is synchronous and queries the engine. If the engine is not selected or unavailable, the references
     * that must be looked up in the engine are reported as {@link ReferenceLocation#NOT_CHECKED not checked} with
     * a warning; other unexpected exceptions are propagated.
     *
     * @param resources all resources of the deployment
     * @return check result
     */
    DeploymentReferenceCheckResult check(List<DeploymentResourceDescriptor> resources);

    /**
     * Performs the part of the {@link #check(List)} that does not need the engine: duplicate keys, references
     * resolved within the deployment, dynamic references and references with the deployment binding. The references
     * that must be looked up in the engine get the {@link ReferenceLocation#CHECKING} location and no message,
     * see {@link DeploymentReferenceCheckResult#isPending()}.
     *
     * @param resources all resources of the deployment
     * @return check result without the engine lookup
     */
    DeploymentReferenceCheckResult checkPending(List<DeploymentResourceDescriptor> resources);

    /**
     * Same as {@link #check(List)}, but the engine is considered unavailable without querying it: the references
     * that must be looked up in the engine are reported as {@link ReferenceLocation#NOT_CHECKED not checked}
     * with a warning. Used as a fallback when the asynchronous engine lookup fails or times out.
     *
     * @param resources all resources of the deployment
     * @return check result without the engine lookup
     */
    DeploymentReferenceCheckResult checkEngineUnavailable(List<DeploymentResourceDescriptor> resources);
}
