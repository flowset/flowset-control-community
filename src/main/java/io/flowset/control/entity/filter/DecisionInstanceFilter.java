/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.filter;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Getter
@Setter
public class DecisionInstanceFilter {

    @JmixGeneratedValue
    @JmixId
    protected UUID id;

    protected String decisionInstanceId;

    protected String decisionDefinitionKey;

    protected String decisionDefinitionId;

    protected String processDefinitionId;

    protected String processDefinitionKey;

    protected String processInstanceId;

    protected String activityId;

    protected OffsetDateTime evaluatedAfter;

    protected OffsetDateTime evaluatedBefore;
}