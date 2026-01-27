package io.flowset.control.mapper;

import io.jmix.core.Metadata;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInputInstanceShortData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionOutputInstanceShortData;
import io.flowset.control.entity.decisioninstance.HistoricDecisionInstanceShortData;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Mapper(componentModel = "spring")
public abstract class DecisionInstanceMapper {

    @Autowired
    Metadata metadata;

    @Mapping(target = "decisionInstanceId", source = "id")
    public abstract HistoricDecisionInstanceShortData fromHistoricDecisionInstance(HistoricDecisionInstance source);

    HistoricDecisionInstanceShortData historicDecisionInstanceShortDataClassFactory() {
        return metadata.create(HistoricDecisionInstanceShortData.class);
    }

    @Mapping(target = "decisionInputInstanceId", source = "id")
    @Mapping(target = "id", ignore = true)
    public abstract HistoricDecisionInputInstanceShortData fromHistoricDecisionInputInstance(HistoricDecisionInputInstance source);

    HistoricDecisionInputInstanceShortData historicDecisionInputInstanceShortDataClassFactory() {
        return metadata.create(HistoricDecisionInputInstanceShortData.class);
    }

    @Mapping(target = "decisionOutputInstanceId", source = "id")
    @Mapping(target = "id", ignore = true)
    public abstract HistoricDecisionOutputInstanceShortData fromHistoricDecisionOutputInstance(HistoricDecisionOutputInstance source);

    HistoricDecisionOutputInstanceShortData historicDecisionOutputInstanceShortDataClassFactory() {
        return metadata.create(HistoricDecisionOutputInstanceShortData.class);
    }

    OffsetDateTime map(Date value) {
        if (value == null) {
            return null;
        }

        return value.toInstant().atOffset(ZoneOffset.UTC);
    }
}
