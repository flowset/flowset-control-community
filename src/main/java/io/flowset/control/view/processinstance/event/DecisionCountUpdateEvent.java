/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.event;

import org.springframework.context.ApplicationEvent;

/**
 * Fires when decision instance count updated.
 */
public class DecisionCountUpdateEvent extends ApplicationEvent {

    private final long count;

    public DecisionCountUpdateEvent(Object source, long count) {
        super(source);
        this.count = count;
    }

    public long getCount() {
        return count;
    }

}
