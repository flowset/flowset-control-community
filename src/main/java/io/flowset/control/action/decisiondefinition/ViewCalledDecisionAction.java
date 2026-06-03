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
import io.flowset.control.view.decisiondefinition.DecisionDefinitionDetailView;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(ViewCalledDecisionAction.ID)
public class ViewCalledDecisionAction extends SecuredBaseAction {

    public static final String ID = "control_viewCalledDecision";

    protected SecuritySupport securitySupport;
    protected BusinessRuleTaskOverlayClickHandler businessRuleTaskClickHandler;

    protected ProcessDefinitionData processDefinitionData;
    protected DecisionReferenceData decisionReference;

    protected boolean enabledByActionUiPermission;

    public ViewCalledDecisionAction() {
        super(ID);
    }

    public ViewCalledDecisionAction(String id) {
        super(id);
    }

    @Autowired
    public void setSecuritySupport(SecuritySupport securitySupport) {
        this.securitySupport = securitySupport;

        enabledByActionUiPermission = securitySupport.isEntityViewPermitted(DecisionDefinitionData.class);
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
    public void actionPerform(Component component) {
        businessRuleTaskClickHandler.handleDecisionNavigation(
                processDefinitionData,
                decisionReference,
                UiComponentUtils.isComponentAttachedToDialog(component));
    }

    @Override
    protected boolean isPermitted() {
        if (decisionReference == null || StringUtils.isEmpty(decisionReference.getDecisionRef())) {
            return false;
        }

        String viewId = ViewDescriptorUtils.getInferredViewId(DecisionDefinitionDetailView.class);
        if (!securitySupport.isShowViewPermitted(viewId)) {
            return false;
        }
        return enabledByActionUiPermission;
    }
}
