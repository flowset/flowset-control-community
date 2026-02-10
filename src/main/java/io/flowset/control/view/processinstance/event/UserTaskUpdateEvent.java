/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.view.processinstance.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * Fires when a user task is updated: completed or reassigned.
 */
@Getter
@Setter
public class UserTaskUpdateEvent extends ApplicationEvent {
    public UserTaskUpdateEvent(Object source) {
        super(source);
    }
}
