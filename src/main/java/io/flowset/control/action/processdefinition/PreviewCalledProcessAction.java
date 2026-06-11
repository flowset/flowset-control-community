/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processdefinition;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.processdefinition.CalledProcessReferenceData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.uicomponent.viewer.handler.CallActivityOverlayClickHandler;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(PreviewCalledProcessAction.ID)
public class PreviewCalledProcessAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_previewCalledProcess";

    protected CallActivityOverlayClickHandler callActivityClickHandler;

    protected ProcessDefinitionData processDefinitionData;
    protected CalledProcessReferenceData calledProcessReference;

    protected boolean visibleByActionUiPermission;

    public PreviewCalledProcessAction() {
        super(ID);
    }

    public PreviewCalledProcessAction(String id) {
        super(id);
    }

    @Autowired
    public void setSecuritySupport(SecuritySupport securitySupport) {
        visibleByActionUiPermission = securitySupport.isEntityViewPermitted(ProcessDefinitionData.class);
    }

    @Autowired
    public void setCallActivityClickHandler(CallActivityOverlayClickHandler callActivityClickHandler) {
        this.callActivityClickHandler = callActivityClickHandler;
    }

    public void setProcessDefinitionData(ProcessDefinitionData processDefinitionData) {
        this.processDefinitionData = processDefinitionData;
    }

    public void setCalledProcessReference(CalledProcessReferenceData calledProcessReference) {
        this.calledProcessReference = calledProcessReference;
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void execute() {
        callActivityClickHandler.handleProcessPreview(processDefinitionData, calledProcessReference);
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }
}
