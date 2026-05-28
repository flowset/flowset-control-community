/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action;

import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.jmix.flowui.action.ActionType;

@ActionType(ViewProcessDefinitionAction.ID)
public class ViewProcessDefinitionAction extends ViewEntityDetailAction {

    public static final String ID = "control_viewProcessDefinition";

    public ViewProcessDefinitionAction() {
        super(ID, ProcessDefinitionData.class);
    }

    public ViewProcessDefinitionAction(String id) {
        super(id, ProcessDefinitionData.class);
    }
}
