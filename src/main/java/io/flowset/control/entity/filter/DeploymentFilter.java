/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.filter;

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

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime deploymentAfter;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime deploymentBefore;

    protected String source;

    protected List<String> tenantIdIn;

    protected Boolean withoutTenantId;
}