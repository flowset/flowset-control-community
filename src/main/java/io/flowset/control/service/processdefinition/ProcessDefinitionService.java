/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processdefinition;

import io.flowset.control.entity.filter.ProcessDefinitionFilter;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.SecuredEntityLoad;
import io.flowset.control.security.SecuredEntityOperation;
import io.flowset.control.security.SpecificPermissions;
import io.jmix.core.security.EntityOp;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Provides methods to manage process definitions and their versions in the BPM engine.
 */
public interface ProcessDefinitionService {

    /**
     * Loads latest versions of the process definitions deployed in the BPM engine.
     *
     * @return a list of deployed process definitions
     */
    @SecuredEntityLoad(entityClass = ProcessDefinitionData.class)
    List<ProcessDefinitionData> findLatestVersions();

    /**
     * Loads process definition versions from the engine using the specified context.
     *
     * @param context a context to load process definitions
     * @return a list of deployed process definition versions
     */
    @SecuredEntityLoad(entityClass = ProcessDefinitionData.class)
    List<ProcessDefinitionData> findAll(ProcessDefinitionLoadContext context);

    /**
     * Loads from engine the total count of process definition versions that match the specified filter.
     *
     * @param filter a process definition filter
     * @return count of deployed process definitions
     */
    @SecuredEntityLoad(entityClass = ProcessDefinitionData.class)
    long getCount(@Nullable ProcessDefinitionFilter filter);

    /**
     * Loads process definition versions from the engine with the specified process key.
     *
     * @param processDefinitionKey a process key
     * @return a list of deployed process definition versions
     */
    @SecuredEntityLoad(entityClass = ProcessDefinitionData.class)
    List<ProcessDefinitionData> findAllByKey(String processDefinitionKey);

    /**
     * Loads a process definition with the specified identifier.
     *
     * @param processDefinitionId a process definition identifier
     * @return found process definition or null if not found
     */
    @Nullable
    @SecuredEntityLoad(entityClass = ProcessDefinitionData.class)
    ProcessDefinitionData getById(String processDefinitionId);

    /**
     * Loads a BPMN 2.0 XML of the process definition with the specified identifier.
     *
     * @param processDefinitionId a process definition identifier
     * @return a process content in the BPMN 2.0 XML format
     */
    @SecuredEntityLoad(entityClass = ProcessDefinitionData.class)
    String getBpmnXml(String processDefinitionId);

    /**
     * Activates a process definition version with the specified identifier.
     * Additionally, all related process instances can be activated as well.
     *
     * @param processDefinitionId      a process definition identifier
     * @param activateBelongsInstances whether activate all process instances related to the specified process definition
     */
    @SecuredEntityOperation(specificPermission = SpecificPermissions.PROCESS_DEFINITION_ACTIVATE)
    void activateById(String processDefinitionId, boolean activateBelongsInstances);

    /**
     * Activates all process definition versions with the specified process key.
     * Additionally, all related process instances can be activated as well.
     *
     * @param processDefinitionKey     a process definition key
     * @param activateBelongsInstances whether activate all process instances related to the all process definitions with key
     */
    @SecuredEntityOperation(specificPermission = SpecificPermissions.PROCESS_DEFINITION_ACTIVATE)
    void activateAllVersionsByKey(String processDefinitionKey, boolean activateBelongsInstances);

    /**
     * Suspends a process definition version with the specified identifier.
     * Additionally, all related process instances can be suspended as well.
     *
     * @param processDefinitionId     a process definition identifier
     * @param suspendBelongsInstances whether suspend all process instances related to the specified process definition
     */
    @SecuredEntityOperation(specificPermission = SpecificPermissions.PROCESS_DEFINITION_SUSPEND)
    void suspendById(String processDefinitionId, boolean suspendBelongsInstances);

    /**
     * Suspends all process definition versions with the specified process key.
     * Additionally, all related process instances can be suspended as well.
     *
     * @param processDefinitionKey    a process definition key
     * @param suspendBelongsInstances whether suspend all process instances related to the all process definitions with key
     */
    @SecuredEntityOperation(specificPermission = SpecificPermissions.PROCESS_DEFINITION_SUSPEND)
    void suspendAllVersionsByKey(String processDefinitionKey, boolean suspendBelongsInstances);

    /**
     * Deletes all process definition versions with the specified process key.
     * Additionally, all related process instances can be deleted as well.
     *
     * @param processDefinitionKey      a process definition key
     * @param deleteAllRelatedInstances whether delete all process instances related to the all process definitions with key
     */
    @SecuredEntityOperation(entityClass = ProcessDefinitionData.class, entityOp = EntityOp.DELETE)
    void deleteAllVersionsByKey(String processDefinitionKey, boolean deleteAllRelatedInstances);

    /**
     * Deletes a process definition version with the specified identifier.
     * Additionally, all related process instances can be deleted as well.
     *
     * @param processDefinitionId       a process definition identifier
     * @param deleteAllRelatedInstances whether delete all process instances related to the specified process definition
     */
    @SecuredEntityOperation(entityClass = ProcessDefinitionData.class, entityOp = EntityOp.DELETE)
    void deleteById(String processDefinitionId, boolean deleteAllRelatedInstances);
}
