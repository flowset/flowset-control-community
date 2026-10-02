/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/**
 * A reference from a BPMN model to another resource: a called process, a decision, a form or a script.
 */
@Getter
@Builder
@ToString
public class ResourceReference {

    public static final String BINDING_LATEST = "latest";
    public static final String BINDING_DEPLOYMENT = "deployment";

    /**
     * Kind of the referenced resource.
     */
    protected final ReferenceKind kind;

    /**
     * Process key, decision key, form key or a resource name (for {@link #byResourceName} references).
     */
    protected final String key;

    /**
     * Raw binding: {@code latest} (default), {@code deployment}, {@code version} or {@code versionTag}.
     */
    protected final String binding;

    /**
     * Whether the resource must be a part of the same deployment.
     */
    protected final boolean deploymentBinding;

    /**
     * Whether the key is an expression ({@code ${...}} or {@code #{...}}) that cannot be checked before the execution.
     */
    protected final boolean dynamic;

    /**
     * Whether the {@link #key} is a resource name instead of a definition key: embedded HTML forms, scripts and
     * Camunda Forms referenced by {@code camunda-forms:deployment:<file>}.
     */
    protected final boolean byResourceName;

    /**
     * Id of the BPMN element that contains the reference, {@code null} if the element has no id.
     */
    @Nullable
    protected final String sourceElementId;

    /**
     * @return whether the value is an expression ({@code ${...}} or {@code #{...}})
     */
    public static boolean isExpression(@Nullable String value) {
        return value != null && (value.contains("${") || value.contains("#{"));
    }
}
