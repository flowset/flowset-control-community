/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.filter;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.PropertyDatatype;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Getter
@Setter
public class BatchFilter {

    @JmixGeneratedValue
    @JmixId
    protected UUID id;

    protected String batchId;

    protected String type;

    protected Boolean suspended;

    protected String createdBy;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime startedBefore;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime startedAfter;

    protected Boolean withFailures;

    protected Boolean withoutFailures;

    protected Boolean completed;
}
