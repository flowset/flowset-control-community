/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.entity.processinstance;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@JmixEntity(name = "bpm_ProcessInstanceData")
public class ProcessInstanceData extends RuntimeProcessInstanceData {

    protected String deleteReason;

    protected String tenantId;

    protected String processDefinitionKey;

    protected String processDefinitionName;

    protected Integer processDefinitionVersion;

    protected String rootProcessInstanceId;

    protected String superProcessInstanceId;

    protected Date endTime;

    protected Date startTime;

    protected Boolean complete = false;

    protected Boolean internallyTerminated = false;

    protected Boolean externallyTerminated = false;

    public Boolean getFinished() {
        return endTime != null;
    }

    @JmixProperty
    public ProcessInstanceState getState() {
        if (complete || internallyTerminated || externallyTerminated) {
            return ProcessInstanceState.COMPLETED;
        }
        if (suspended) {
            return ProcessInstanceState.SUSPENDED;
        }
        return ProcessInstanceState.ACTIVE;
    }

    @InstanceName
    @DependsOnProperties({"instanceId"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("Process Instance %s",
                metadataTools.format(instanceId));
    }
}
