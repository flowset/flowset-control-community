/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of the reference check of a deployment being prepared.
 */
@Getter
public class DeploymentReferenceCheckResult {

    /**
     * All references of the deployment resources with their locations.
     */
    protected final List<ResolvedReference> references = new ArrayList<>();

    /**
     * Localized errors by file name: missing resources with the deployment binding, duplicate keys.
     */
    protected final Map<String, List<String>> errors = new LinkedHashMap<>();

    /**
     * Localized warnings by file name.
     */
    protected final Map<String, List<String>> warnings = new LinkedHashMap<>();

    public void addReference(ResolvedReference reference) {
        references.add(reference);
        switch (reference.severity()) {
            case ERROR -> addError(reference.fileName(), reference.message());
            case WARNING -> addWarning(reference.fileName(), reference.message());
            case OK -> {
            }
        }
    }

    public void addError(String fileName, String error) {
        errors.computeIfAbsent(fileName, name -> new ArrayList<>()).add(error);
    }

    public void addWarning(String fileName, String warning) {
        warnings.computeIfAbsent(fileName, name -> new ArrayList<>()).add(warning);
    }

    public List<String> getErrors(String fileName) {
        return errors.getOrDefault(fileName, List.of());
    }

    public List<String> getWarnings(String fileName) {
        return warnings.getOrDefault(fileName, List.of());
    }

    /**
     * @return whether some references are still being checked in the engine
     */
    public boolean isPending() {
        return references.stream().anyMatch(reference -> reference.location() == ReferenceLocation.CHECKING);
    }
}
