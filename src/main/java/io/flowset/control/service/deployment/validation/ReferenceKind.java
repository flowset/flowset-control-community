/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

/**
 * Kind of a resource referenced from a BPMN model.
 */
public enum ReferenceKind implements EnumClass<String> {

    /**
     * A process called by a call activity ({@code calledElement}).
     */
    PROCESS("process"),
    /**
     * A decision called by a business rule task ({@code camunda:decisionRef}).
     */
    DECISION("decision"),
    /**
     * A Camunda Form referenced by a user task or a start event ({@code camunda:formRef}
     * or {@code camunda:formKey="camunda-forms:deployment:..."}).
     */
    CAMUNDA_FORM("camundaForm"),
    /**
     * An embedded HTML form ({@code camunda:formKey="embedded:deployment:..."}).
     */
    HTML_FORM("htmlForm"),
    /**
     * An external script resource ({@code deployment://...}).
     */
    SCRIPT("script");

    private final String id;

    ReferenceKind(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static ReferenceKind fromId(@Nullable String id) {
        for (ReferenceKind value : ReferenceKind.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
