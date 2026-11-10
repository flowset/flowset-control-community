/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.deployment;

import com.vaadin.flow.component.Component;
import io.flowset.control.security.accesscontext.decisiondefinition.DecisionDefinitionDeployAccessContext;
import io.flowset.control.security.accesscontext.processdefinition.ProcessDefinitionDeployAccessContext;
import io.flowset.control.view.deploymentupload.DeploymentUploadView;
import io.jmix.core.AccessManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

/**
 * Opens the {@link DeploymentUploadView}. The action is visible only to the users that are permitted to deploy
 * processes or decisions.
 */
@ActionType(UploadDeploymentAction.ID)
public class UploadDeploymentAction extends SecuredBaseAction<UploadDeploymentAction> implements ExecutableAction {

    public static final String ID = "control_uploadDeployment";

    protected ViewNavigators viewNavigators;

    protected boolean visibleByActionUiPermission;

    public UploadDeploymentAction() {
        super(ID);
    }

    public UploadDeploymentAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessDefinitionDeployAccessContext processContext = new ProcessDefinitionDeployAccessContext();
        accessManager.applyRegisteredConstraints(processContext);

        DecisionDefinitionDeployAccessContext decisionContext = new DecisionDefinitionDeployAccessContext();
        accessManager.applyRegisteredConstraints(decisionContext);

        visibleByActionUiPermission = processContext.isPermitted() || decisionContext.isPermitted();
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Override
    public void execute() {
        viewNavigators.view(getCurrentView(), DeploymentUploadView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }
}
