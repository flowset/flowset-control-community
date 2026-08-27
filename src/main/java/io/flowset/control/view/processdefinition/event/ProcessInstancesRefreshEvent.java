/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processdefinition.event;

import io.flowset.control.view.processdefinition.ProcessDefinitionDetailView;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * Event for refreshing process instances data in {@link ProcessDefinitionDetailView}
 */
@Getter
@Setter
public class ProcessInstancesRefreshEvent extends ApplicationEvent {
    protected boolean terminate;

    public ProcessInstancesRefreshEvent(Object source, boolean terminate) {
        super(source);
        this.terminate = terminate;
    }
}
