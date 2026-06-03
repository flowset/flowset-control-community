/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.engine;

import io.flowset.control.entity.engine.BpmEngine;
import io.flowset.control.security.SecuritySupport;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(CreateBpmEngineAction.ID)
public class CreateBpmEngineAction extends SecuredBaseAction {

    public static final String ID = "control_createBpmEngine";

    protected boolean visibleByCreatePermission;

    public CreateBpmEngineAction() {
        super(ID);
    }

    public CreateBpmEngineAction(String id) {
        super(id);
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByCreatePermission);
    }

    @Autowired
    protected void setSecuritySupport(SecuritySupport securitySupport) {
        visibleByCreatePermission = securitySupport.isEntityCreatePermitted(BpmEngine.class);
    }
}
