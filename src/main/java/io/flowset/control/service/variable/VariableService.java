/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.variable;

import io.flowset.control.entity.filter.VariableFilter;
import io.flowset.control.entity.variable.HistoricVariableInstanceData;
import io.flowset.control.entity.variable.VariableInstanceData;
import io.flowset.control.security.SecuredEntityLoad;
import io.flowset.control.security.SecuredEntityOperation;
import io.jmix.core.security.EntityOp;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Provides methods for managing process variable instances in the BPM engine.
 */
public interface VariableService {

    /**
     * Loads variable instances from the engine runtime data using the specified context.
     *
     * @param loadContext a context to load process variable instances
     * @return a list of variable instances
     */
    @SecuredEntityLoad(entityClass = VariableInstanceData.class)
    List<VariableInstanceData> findRuntimeVariables(VariableLoadContext loadContext);

    /**
     * Loads process variable instance with the specified identifier
     * from the engine runtime data.
     *
     * @param variableInstanceId identifier of the process variable instance
     * @return found process variable instance
     */
    @SecuredEntityLoad(entityClass = VariableInstanceData.class)
    VariableInstanceData findRuntimeVariableById(String variableInstanceId);

    /**
     * Loads the binary value of the process variable instance
     * with the specified identifier.
     *
     * @param variableInstanceId identifier of the process variable instance
     * @return resource containing binary data of the variable instance
     */
    @SecuredEntityLoad(entityClass = VariableInstanceData.class)
    Resource getVariableInstanceBinary(String variableInstanceId);

    /**
     * Loads variable instances from the engine history using the specified context.
     *
     * @param loadContext a context to load variable instances
     * @return a list of process variable instances
     */
    @SecuredEntityLoad(entityClass = HistoricVariableInstanceData.class)
    List<HistoricVariableInstanceData> findHistoricVariables(VariableLoadContext loadContext);

    /**
     * Loads a total count of process variable instances from the engine runtime data that match the specified filter.
     *
     * @param filter variable instance filter
     * @return a count of variable instances
     */
    @SecuredEntityLoad(entityClass = VariableInstanceData.class)
    long getRuntimeVariablesCount(@Nullable VariableFilter filter);

    /**
     * Loads a total count of process variable instances from the engine history that match the specified filter.
     *
     * @param filter variable instance filter
     * @return a count of process variable instances
     */
    @SecuredEntityLoad(entityClass = HistoricVariableInstanceData.class)
    long getHistoricVariablesCount(@Nullable VariableFilter filter);

    /**
     * Updates the value of the specified process variable to the specified value.
     *
     * @param variableInstanceData variable instance data containing new value
     */
    @SecuredEntityOperation(entityClass = VariableInstanceData.class, entityOp = EntityOp.UPDATE)
    void updateVariableLocal(VariableInstanceData variableInstanceData);

    /**
     * Loads process variable instance with the specified identifier from the engine history.
     *
     * @param variableInstanceId variable instance identifier
     * @return found process variable instance
     */
    @SecuredEntityLoad(entityClass = HistoricVariableInstanceData.class)
    HistoricVariableInstanceData findHistoricVariableById(String variableInstanceId);

    /**
     * Removes the specified process variable instance from the engine runtime data.
     *
     * @param variableInstanceData process variable instance to be removed
     */
    @SecuredEntityOperation(entityClass = VariableInstanceData.class, entityOp = EntityOp.DELETE)
    void removeVariableLocal(VariableInstanceData variableInstanceData);

    /**
     * Removes the specified set of process variable instances
     * from the engine runtime data within the given execution context.
     *
     * @param executionId   identifier of the process execution context
     * @param variableItems set of process variable instances to be removed
     */
    @SecuredEntityOperation(entityClass = VariableInstanceData.class, entityOp = EntityOp.DELETE)
    void removeVariablesLocal(String executionId, Set<VariableInstanceData> variableItems);

    /**
     * Updates the binary value of the specified process variable instance
     * with the provided file data.
     *
     * @param variableInstanceData process variable instance to update
     * @param data                 file containing new binary value
     */
    @SecuredEntityOperation(entityClass = VariableInstanceData.class, entityOp = EntityOp.UPDATE)
    void updateVariableBinary(VariableInstanceData variableInstanceData, File data);
}
