/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.filter;

import io.flowset.control.annotation.EngineProperty;
import io.flowset.control.entity.engine.EngineFamily;
import io.flowset.control.entity.processdefinition.ProcessDefinitionState;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Getter
@Setter
public class ProcessDefinitionFilter {
    @JmixGeneratedValue
    @JmixId
    protected UUID id;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected List<String> keyIn;

    protected String state;

    protected String keyLike;

    protected String key;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected List<String> idIn;

    protected String deploymentId;

    @InstanceName
    protected String nameLike;

    protected Boolean latestVersionOnly = true;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected String versionTag;

    protected Integer version;

    public ProcessDefinitionState getState() {
        return state == null ? null : ProcessDefinitionState.fromId(state);
    }

    public void setState(ProcessDefinitionState state) {
        this.state = state == null ? null : state.getId();
    }
}
