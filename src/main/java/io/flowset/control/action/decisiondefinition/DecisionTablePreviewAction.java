/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.decisiondefinition;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.decisiondefinition.DecisionDefinitionData;
import io.flowset.control.entity.decisiondefinition.DecisionReferenceData;
import io.flowset.control.entity.processdefinition.ProcessDefinitionData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.uicomponent.viewer.handler.BusinessRuleTaskOverlayClickHandler;
import io.flowset.control.view.decisiondefinition.DecisionDefinitionDiagramView;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DecisionTablePreviewAction.ID)
public class DecisionTablePreviewAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_decisionTablePreview";

    protected SecuritySupport securitySupport;
    protected BusinessRuleTaskOverlayClickHandler businessRuleTaskClickHandler;

    protected ProcessDefinitionData processDefinitionData;
    protected DecisionReferenceData decisionReference;

    protected boolean visibleByActionUiPermission;

    public DecisionTablePreviewAction() {
        super(ID);
    }

    public DecisionTablePreviewAction(String id) {
        super(id);
    }

    @Autowired
    public void setSecuritySupport(SecuritySupport securitySupport) {
        this.securitySupport = securitySupport;

        visibleByActionUiPermission = securitySupport.isEntityViewPermitted(DecisionDefinitionData.class);
    }

    @Autowired
    public void setBusinessRuleTaskClickHandler(BusinessRuleTaskOverlayClickHandler businessRuleTaskClickHandler) {
        this.businessRuleTaskClickHandler = businessRuleTaskClickHandler;
    }

    public void setProcessDefinitionData(ProcessDefinitionData processDefinitionData) {
        this.processDefinitionData = processDefinitionData;
    }

    public void setDecisionReference(DecisionReferenceData decisionReference) {
        this.decisionReference = decisionReference;

        refreshState();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission && isViewPermitted());
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void execute() {
        businessRuleTaskClickHandler.handleDecisionPreview(processDefinitionData, decisionReference);
    }

    @Override
    protected boolean isPermitted() {
        if (decisionReference == null || StringUtils.isEmpty(decisionReference.getDecisionRef())) {
            return false;
        }

        return super.isPermitted();
    }

    protected boolean isViewPermitted() {
        String viewId = ViewDescriptorUtils.getInferredViewId(DecisionDefinitionDiagramView.class);
        return securitySupport.isShowViewPermitted(viewId);
    }
}
