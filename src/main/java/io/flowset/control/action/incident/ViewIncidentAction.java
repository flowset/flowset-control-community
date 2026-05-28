/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.incident;

import io.flowset.control.action.ViewEntityDetailAction;
import io.flowset.control.entity.incident.IncidentData;
import io.jmix.flowui.action.ActionType;

@ActionType(ViewIncidentAction.ID)
public class ViewIncidentAction extends ViewEntityDetailAction {

    public static final String ID = "control_viewIncident";

    public ViewIncidentAction() {
        super(ID, IncidentData.class);
    }

    public ViewIncidentAction(String id) {
        super(id, IncidentData.class);
    }
}
