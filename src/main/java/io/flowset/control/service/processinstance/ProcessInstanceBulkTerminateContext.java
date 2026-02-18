/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.service.processinstance;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Context for bulk terminating process instances.
 */
@Getter
@Setter
@Accessors(chain = true)
public class ProcessInstanceBulkTerminateContext {
    protected List<String> processInstanceIds;
    protected String reason;
    protected Boolean skipCustomListeners;
    protected Boolean skipSubprocesses;
    protected Boolean skipIoMappings;

    public ProcessInstanceBulkTerminateContext(List<String> processInstanceIds) {
        this.processInstanceIds = processInstanceIds;
    }
}
