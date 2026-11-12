/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.decisiondefinition;

import io.flowset.control.annotation.EngineProperty;
import io.flowset.control.entity.engine.EngineFamily;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import lombok.Getter;
import lombok.Setter;

@JmixEntity(name = "bpm_DecisionDefinitionData")
@Getter
@Setter
public class DecisionDefinitionData {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    protected String decisionDefinitionId;

    protected String name;

    protected String key;

    protected String category;

    protected String deploymentId;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected Integer historyTimeToLive;

    protected String resourceName;

    protected String tenantId;

    protected Integer version;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected String versionTag;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected String decisionRequirementsDefinitionId;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected String decisionRequirementsDefinitionKey;
}
