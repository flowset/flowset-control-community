/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.variable;

import io.jmix.core.CopyingSystemState;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Transient;

@JmixEntity(annotatedPropertiesOnly = true)
public class VariableInstanceData implements CopyingSystemState<VariableInstanceData> {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    @JmixProperty
    protected VariableValueInfo valueInfo;

    @JmixProperty
    protected String type;

    @JmixProperty
    protected String variableInstanceId;

    @JmixProperty
    protected String name;


    @Nullable
    @JmixProperty
    protected String executionId;

    @Nullable
    @JmixProperty
    protected String activityInstanceId;

    @JmixProperty
    protected String errorMessage;

    @JmixProperty
    protected Boolean local;

    @Transient
    protected Object value;

    public Boolean getLocal() {
        return local;
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public VariableValueInfo getValueInfo() {
        return valueInfo;
    }

    public void setValueInfo(VariableValueInfo valueInfo) {
        this.valueInfo = valueInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVariableInstanceId() {
        return variableInstanceId;
    }

    public void setVariableInstanceId(String variableInstanceId) {
        this.variableInstanceId = variableInstanceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Nullable
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(@Nullable String executionId) {
        this.executionId = executionId;
    }

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(@Nullable String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    @InstanceName
    @DependsOnProperties({"name"})
    public String getInstanceName() {
        return name;
    }

    @Override
    public void copyFrom(VariableInstanceData source) {
        this.value = source.value;
    }
}