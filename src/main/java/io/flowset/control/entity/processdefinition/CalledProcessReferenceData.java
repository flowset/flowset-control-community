/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.processdefinition;

import io.flowset.uikit.component.bpmnviewer.model.CallActivityData;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JmixEntity
@Getter
@Setter
public class CalledProcessReferenceData extends CallActivityData {

    @JmixGeneratedValue
    @JmixId
    protected UUID id;
}
