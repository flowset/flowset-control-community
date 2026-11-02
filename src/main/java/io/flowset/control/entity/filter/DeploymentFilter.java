/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.filter;

import io.flowset.control.annotation.EngineProperty;
import io.flowset.control.datatype.EngineOffsetDateTimeDatatype;
import io.flowset.control.entity.engine.EngineFamily;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.PropertyDatatype;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Getter
@Setter
public class DeploymentFilter {

    @JmixGeneratedValue
    @JmixId
    protected UUID id;

    protected String deploymentId;

    @InstanceName
    protected String nameLike;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    @PropertyDatatype(value = EngineOffsetDateTimeDatatype.DATATYPE_NAME)
    protected OffsetDateTime deploymentAfter;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    @PropertyDatatype(value = EngineOffsetDateTimeDatatype.DATATYPE_NAME)
    protected OffsetDateTime deploymentBefore;

    @EngineProperty(supportedFamilies = EngineFamily.CAMUNDA_7)
    protected String source;

    protected List<String> tenantIdIn;

    protected Boolean withoutTenantId;
}
