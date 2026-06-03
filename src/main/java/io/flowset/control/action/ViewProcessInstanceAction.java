/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action;

import io.flowset.control.entity.processinstance.ProcessInstanceData;
import io.jmix.flowui.action.ActionType;

@ActionType(ViewProcessInstanceAction.ID)
public class ViewProcessInstanceAction extends ViewEntityDetailAction {

    public static final String ID = "control_viewProcessInstance";

    public ViewProcessInstanceAction() {
        super(ID, ProcessInstanceData.class);
    }

    public ViewProcessInstanceAction(String id) {
        super(id, ProcessInstanceData.class);
    }
}
