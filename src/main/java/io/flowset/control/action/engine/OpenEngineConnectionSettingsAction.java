/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.engine;

import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.SecuredBaseAction;

@ActionType(OpenEngineConnectionSettingsAction.ID)
public class OpenEngineConnectionSettingsAction extends SecuredBaseAction {

    public static final String ID = "control_openEngineConnectionSettings";

    public OpenEngineConnectionSettingsAction() {
        super(ID);
    }

    public OpenEngineConnectionSettingsAction(String id) {
        super(id);
    }
}
