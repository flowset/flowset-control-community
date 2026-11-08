/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.deployment.validation;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.deployment.DeploymentResourceType;
import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.service.decisiondefinition.DecisionDefinitionService;
import io.flowset.control.service.engine.EngineService;
import io.flowset.control.service.processdefinition.ProcessDefinitionLoadContext;
import io.flowset.control.service.processdefinition.ProcessDefinitionService;
import io.jmix.core.Messages;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service("control_DeploymentReferenceChecker")
@Slf4j
public class DeploymentReferenceCheckerImpl implements DeploymentReferenceChecker {

    protected static final String MESSAGE_GROUP = DeploymentResourceValidatorImpl.MESSAGE_GROUP;

    protected final ProcessDefinitionService processDefinitionService;
    protected final DecisionDefinitionService decisionDefinitionService;
    protected final EngineService engineService;
    protected final Messages messages;

    public DeploymentReferenceCheckerImpl(ProcessDefinitionService processDefinitionService,
                                          DecisionDefinitionService decisionDefinitionService,
                                          EngineService engineService,
                                          Messages messages) {
        this.processDefinitionService = processDefinitionService;
        this.decisionDefinitionService = decisionDefinitionService;
        this.engineService = engineService;
        this.messages = messages;
    }

    @Override
    public DeploymentReferenceCheckResult check(List<DeploymentResourceDescriptor> resources) {
        return check(resources, this::lookupInEngine);
    }

    @Override
    public DeploymentReferenceCheckResult checkPending(List<DeploymentResourceDescriptor> resources) {
        return check(resources, (processKeys, decisionKeys) -> EngineLookup.PENDING);
    }

    @Override
    public DeploymentReferenceCheckResult checkEngineUnavailable(List<DeploymentResourceDescriptor> resources) {
        return check(resources, (processKeys, decisionKeys) -> EngineLookup.UNAVAILABLE);
    }

    /**
     * Checks the duplicate keys (never depends on the engine) and resolves the references; the references that
     * must be looked up in the engine are resolved by the passed engine lookup.
     */
    protected DeploymentReferenceCheckResult check(List<DeploymentResourceDescriptor> resources,
                                                   EngineLookupFunction engineLookupFunction) {
        DeploymentReferenceCheckResult result = new DeploymentReferenceCheckResult();
        checkDuplicateKeys(resources, result);
        resolveReferences(resources, result, engineLookupFunction);
        return result;
    }

    /**
     * Reports the process, decision and form keys that occur more than once within the deployment.
     */
    protected void checkDuplicateKeys(List<DeploymentResourceDescriptor> resources,
                                      DeploymentReferenceCheckResult result) {
        checkDuplicateKeys(resources, DeploymentResourceType.BPMN, ReferenceKind.PROCESS, result);
        checkDuplicateKeys(resources, DeploymentResourceType.DMN, ReferenceKind.DECISION, result);
        checkDuplicateKeys(resources, DeploymentResourceType.CAMUNDA_FORM, ReferenceKind.CAMUNDA_FORM, result);
    }

    protected void checkDuplicateKeys(List<DeploymentResourceDescriptor> resources, DeploymentResourceType type,
                                      ReferenceKind kind, DeploymentReferenceCheckResult result) {
        // key -> names of the files containing the key, one entry per occurrence
        Map<String, List<String>> filesByKey = new LinkedHashMap<>();
        for (DeploymentResourceDescriptor resource : resources) {
            if (resource.type() == type) {
                resource.keys().forEach(key ->
                        filesByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(resource.fileName()));
            }
        }
        filesByKey.forEach((key, fileNames) -> {
            if (fileNames.size() < 2) {
                return;
            }
            for (String fileName : new LinkedHashSet<>(fileNames)) {
                List<String> otherFiles = fileNames.stream()
                        .filter(name -> !name.equals(fileName))
                        .distinct()
                        .toList();
                // the key is duplicated within a single file
                String files = otherFiles.isEmpty() ? fileName : String.join(", ", otherFiles);
                result.addError(fileName, formatMessage("duplicateKey", getKindLabel(kind), key, files));
            }
        });
    }

    protected void resolveReferences(List<DeploymentResourceDescriptor> resources,
                                     DeploymentReferenceCheckResult result,
                                     EngineLookupFunction engineLookupFunction) {
        DeploymentIndex index = new DeploymentIndex(resources);

        // the references that are not found in the deployment and must be looked up in the engine
        Set<String> processKeys = new LinkedHashSet<>();
        Set<String> decisionKeys = new LinkedHashSet<>();
        for (DeploymentResourceDescriptor resource : resources) {
            for (ResourceReference reference : resource.references()) {
                if (requiresEngineLookup(reference, index)) {
                    if (reference.getKind() == ReferenceKind.PROCESS) {
                        processKeys.add(reference.getKey());
                    } else if (reference.getKind() == ReferenceKind.DECISION) {
                        decisionKeys.add(reference.getKey());
                    }
                }
            }
        }
        EngineLookup engineLookup = engineLookupFunction.lookup(processKeys, decisionKeys);

        for (DeploymentResourceDescriptor resource : resources) {
            for (ResourceReference reference : resource.references()) {
                result.addReference(resolve(resource.fileName(), reference, index, engineLookup));
            }
        }
    }

    protected boolean requiresEngineLookup(ResourceReference reference, DeploymentIndex index) {
        return !reference.isDynamic()
                && !reference.isDeploymentBinding()
                && (reference.getKind() == ReferenceKind.PROCESS || reference.getKind() == ReferenceKind.DECISION)
                && findInDeployment(reference, index) == null;
    }

    protected ResolvedReference resolve(String fileName, ResourceReference reference, DeploymentIndex index,
                                        EngineLookup engineLookup) {
        String kindLabel = getKindLabel(reference.getKind());
        String key = reference.getKey();

        String foundResource = findInDeployment(reference, index);
        if (foundResource != null) {
            if (reference.isByResourceName() && !foundResource.equals(key)) {
                return new ResolvedReference(fileName, reference, ReferenceLocation.THIS_DEPLOYMENT,
                        DeploymentResourceStatus.WARNING,
                        formatMessage("referencePathDiffers", kindLabel, key, foundResource));
            }
            return new ResolvedReference(fileName, reference, ReferenceLocation.THIS_DEPLOYMENT,
                    DeploymentResourceStatus.OK, "");
        }
        if (reference.isDynamic()) {
            return new ResolvedReference(fileName, reference, ReferenceLocation.NOT_CHECKED,
                    DeploymentResourceStatus.WARNING, formatMessage("referenceDynamic", kindLabel, key));
        }
        if (reference.isDeploymentBinding()) {
            return new ResolvedReference(fileName, reference, ReferenceLocation.MISSING,
                    DeploymentResourceStatus.ERROR, formatMessage("referenceMissingInDeployment", kindLabel, key));
        }

        Set<String> engineKeys = switch (reference.getKind()) {
            case PROCESS -> engineLookup.processKeys();
            case DECISION -> engineLookup.decisionKeys();
            default -> null;
        };
        if (engineKeys == null) {
            // Camunda 7 REST API does not support looking up forms and resources by key or name. Such references
            // cannot be checked by design, so they are not warnings: the message is shown in the references grid only
            return new ResolvedReference(fileName, reference, ReferenceLocation.NOT_CHECKED,
                    DeploymentResourceStatus.OK, formatMessage("referenceNotCheckedInEngine", kindLabel, key));
        }
        if (engineLookup.pending()) {
            return new ResolvedReference(fileName, reference, ReferenceLocation.CHECKING,
                    DeploymentResourceStatus.OK, "");
        }
        if (!engineLookup.available()) {
            return new ResolvedReference(fileName, reference, ReferenceLocation.NOT_CHECKED,
                    DeploymentResourceStatus.WARNING, formatMessage("referenceEngineUnavailable", kindLabel, key));
        }
        if (engineKeys.contains(key)) {
            return new ResolvedReference(fileName, reference, ReferenceLocation.ENGINE,
                    DeploymentResourceStatus.OK, "");
        }
        return new ResolvedReference(fileName, reference, ReferenceLocation.MISSING,
                DeploymentResourceStatus.WARNING, formatMessage("referenceMissing", kindLabel, key));
    }

    /**
     * Searches the referenced resource in the deployment. Definitions are matched by key. Resources are matched by
     * the exact resource name or, if not found, by the last segment of the referenced path.
     *
     * @return the matched key or resource name, {@code null} if not found
     */
    @Nullable
    protected String findInDeployment(ResourceReference reference, DeploymentIndex index) {
        String key = reference.getKey();
        if (reference.isByResourceName()) {
            if (index.fileNames().contains(key)) {
                return key;
            }
            String lastSegment = StringUtils.substringAfterLast("/" + key.replace('\\', '/'), "/");
            return index.fileNames().contains(lastSegment) ? lastSegment : null;
        }
        Set<String> keys = switch (reference.getKind()) {
            case PROCESS -> index.processKeys();
            case DECISION -> index.decisionKeys();
            case CAMUNDA_FORM -> index.formKeys();
            case HTML_FORM, SCRIPT -> Set.of();
        };
        return keys.contains(key) ? key : null;
    }

    /**
     * Looks up the process and decision keys in the selected engine: one request for all processes and one request
     * per decision key (the Camunda 7 REST API does not support filtering decisions by a list of keys).
     */
    protected EngineLookup lookupInEngine(Set<String> processKeys, Set<String> decisionKeys) {
        if (processKeys.isEmpty() && decisionKeys.isEmpty()) {
            return EngineLookup.EMPTY;
        }
        try {
            if (engineService.getSelectedEngine() == null) {
                log.warn("Unable to check the deployment references in the engine: BPM engine not selected");
                return EngineLookup.UNAVAILABLE;
            }
            return new EngineLookup(true, false, findProcessKeys(processKeys), findDecisionKeys(decisionKeys));
        } catch (RuntimeException e) {
            // all engine-dependent references become not checked
            log.warn("Unable to check the deployment references in the engine: {}", e.getMessage());
            log.debug("Engine lookup failure", e);
            return EngineLookup.UNAVAILABLE;
        }
    }

    protected Set<String> findProcessKeys(Set<String> keys) {
        if (keys.isEmpty()) {
            return Set.of();
        }
        ProcessDefinitionFilter filter = new ProcessDefinitionFilter();
        filter.setKeyIn(List.copyOf(keys));
        filter.setLatestVersionOnly(true);
        List<ProcessDefinitionData> definitions = processDefinitionService.findAll(
                new ProcessDefinitionLoadContext().setFilter(filter));
        Set<String> result = new HashSet<>();
        for (ProcessDefinitionData definition : Objects.requireNonNullElse(definitions,
                List.<ProcessDefinitionData>of())) {
            if (definition.getKey() != null) {
                result.add(definition.getKey());
            }
        }
        return result;
    }

    protected Set<String> findDecisionKeys(Set<String> keys) {
        Set<String> result = new HashSet<>();
        for (String key : keys) {
            List<DecisionDefinitionData> definitions = decisionDefinitionService.findAllByKey(key);
            if (definitions != null && !definitions.isEmpty()) {
                result.add(key);
            }
        }
        return result;
    }

    protected String getKindLabel(ReferenceKind kind) {
        return messages.getMessage(kind);
    }

    protected String formatMessage(String key, Object... params) {
        return messages.formatMessage(MESSAGE_GROUP, key, params);
    }

    /**
     * Keys and resource names of the deployment being prepared.
     */
    protected record DeploymentIndex(Set<String> processKeys, Set<String> decisionKeys, Set<String> formKeys,
                                     Set<String> fileNames) {

        protected DeploymentIndex(List<DeploymentResourceDescriptor> resources) {
            this(collectKeys(resources, DeploymentResourceType.BPMN),
                    collectKeys(resources, DeploymentResourceType.DMN),
                    collectKeys(resources, DeploymentResourceType.CAMUNDA_FORM),
                    collectFileNames(resources));
        }

        private static Set<String> collectKeys(List<DeploymentResourceDescriptor> resources,
                                               DeploymentResourceType type) {
            Set<String> result = new HashSet<>();
            resources.stream()
                    .filter(resource -> resource.type() == type)
                    .forEach(resource -> result.addAll(resource.keys()));
            return result;
        }

        private static Set<String> collectFileNames(List<DeploymentResourceDescriptor> resources) {
            Set<String> result = new HashSet<>();
            resources.forEach(resource -> result.add(resource.fileName()));
            return result;
        }
    }

    /**
     * Result of the engine lookup.
     *
     * @param available    whether the engine has been queried successfully
     * @param pending      whether the engine lookup is not performed yet (it is performed asynchronously)
     * @param processKeys  found process keys
     * @param decisionKeys found decision keys
     */
    protected record EngineLookup(boolean available, boolean pending, Set<String> processKeys,
                                  Set<String> decisionKeys) {

        /**
         * Nothing to look up.
         */
        protected static final EngineLookup EMPTY = new EngineLookup(true, false, Set.of(), Set.of());
        /**
         * The engine is not selected or unavailable.
         */
        protected static final EngineLookup UNAVAILABLE = new EngineLookup(false, false, Set.of(), Set.of());
        /**
         * The engine lookup is not performed yet.
         */
        protected static final EngineLookup PENDING = new EngineLookup(false, true, Set.of(), Set.of());
    }

    /**
     * Looks up in the engine the process and decision keys that are not found in the deployment.
     */
    @FunctionalInterface
    protected interface EngineLookupFunction {

        EngineLookup lookup(Set<String> processKeys, Set<String> decisionKeys);
    }
}
