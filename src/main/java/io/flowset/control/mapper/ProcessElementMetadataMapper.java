/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.mapper;

import io.flowset.control.entity.processdefinition.CalledProcessReferenceData;
import io.flowset.control.entity.decisiondefinition.DecisionReferenceData;
import io.flowset.uikit.component.bpmnviewer.model.BusinessRuleTaskData;
import io.flowset.uikit.component.bpmnviewer.model.CallActivityData;
import io.jmix.core.Metadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ProcessElementMetadataMapper {
    @Autowired
    Metadata metadata;

    public abstract List<CalledProcessReferenceData> fromCallActivities(List<CallActivityData> callActivityDataList);

    public abstract List<DecisionReferenceData> fromBusinessRuleTasks(List<BusinessRuleTaskData> decisionReferenceDataList);

    @Mapping(target = "id", ignore = true)
    public abstract CalledProcessReferenceData fromCallActivity(CallActivityData source);

    @Mapping(target = "id", ignore = true)
    public abstract DecisionReferenceData fromBusinessRuleTask(BusinessRuleTaskData source);

    CalledProcessReferenceData calledProcessReferenceDataClassFactory() {
        return metadata.create(CalledProcessReferenceData.class);
    }

    DecisionReferenceData decisionReferenceDataClassFactory() {
        return metadata.create(DecisionReferenceData.class);
    }
}
