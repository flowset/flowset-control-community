/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.batch;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.PropertyDatatype;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;

import java.time.OffsetDateTime;

@JmixEntity
@Getter
@Setter
public class BatchData extends RuntimeBatchData {

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime endTime;

    @PropertyDatatype(value = "engineOffsetDateTime")
    protected OffsetDateTime removalTime;

    @JmixProperty
    public BatchState getState() {
        if (endTime != null) {
            return BatchState.COMPLETED;
        }
        return BooleanUtils.isTrue(suspended) ? BatchState.SUSPENDED : BatchState.ACTIVE;
    }
}
