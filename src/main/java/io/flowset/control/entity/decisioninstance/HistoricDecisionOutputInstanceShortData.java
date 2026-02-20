package io.flowset.control.entity.decisioninstance;

import io.jmix.core.CopyingSystemState;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.Transient;

@JmixEntity(annotatedPropertiesOnly = true)
public class HistoricDecisionOutputInstanceShortData implements CopyingSystemState<HistoricDecisionOutputInstanceShortData> {

    @JmixId
    @JmixProperty(mandatory = true)
    protected String id;

    @JmixProperty
    protected String decisionOutputInstanceId;

    @JmixProperty
    protected String decisionInstanceId;

    @JmixProperty
    protected String clauseId;

    @JmixProperty
    protected String ruleId;

    @JmixProperty
    protected String clauseName;

    @JmixProperty
    protected String typeName;

    @Transient
    protected Object value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDecisionOutputInstanceId() {
        return decisionOutputInstanceId;
    }

    public void setDecisionOutputInstanceId(String decisionOutputInstanceId) {
        this.decisionOutputInstanceId = decisionOutputInstanceId;
    }

    public String getDecisionInstanceId() {
        return decisionInstanceId;
    }

    public void setDecisionInstanceId(String decisionInstanceId) {
        this.decisionInstanceId = decisionInstanceId;
    }

    public String getClauseId() {
        return clauseId;
    }

    public void setClauseId(String clauseId) {
        this.clauseId = clauseId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getClauseName() {
        return clauseName;
    }

    public void setClauseName(String clauseName) {
        this.clauseName = clauseName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public void copyFrom(HistoricDecisionOutputInstanceShortData source) {
        this.value = source.value;
    }
}
