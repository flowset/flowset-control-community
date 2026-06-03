/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action;

import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.jmix.flowui.action.ActionType;

@ActionType(ViewDecisionDefinitionAction.ID)
public class ViewDecisionDefinitionAction extends ViewEntityDetailAction {

    public static final String ID = "control_viewDecisionDefinition";

    public ViewDecisionDefinitionAction() {
        super(ID, DecisionDefinitionData.class);
    }

    public ViewDecisionDefinitionAction(String id) {
        super(id, DecisionDefinitionData.class);
    }
}
