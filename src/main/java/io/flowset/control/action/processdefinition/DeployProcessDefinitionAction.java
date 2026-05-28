/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.processdefinition;

import com.vaadin.flow.component.Component;
import io.flowset.control.security.accesscontext.processdefinition.ProcessDefinitionDeployAccessContext;
import io.flowset.control.view.newprocessdeployment.NewProcessDeploymentView;
import io.jmix.core.AccessManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(DeployProcessDefinitionAction.ID)
public class DeployProcessDefinitionAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_deployProcessDefinition";

    protected ViewNavigators viewNavigators;

    protected boolean visibleByActionUiPermission;

    public DeployProcessDefinitionAction() {
        super(ID);
    }

    public DeployProcessDefinitionAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        ProcessDefinitionDeployAccessContext context = new ProcessDefinitionDeployAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Override
    public void execute() {
        viewNavigators.view(getCurrentView(), NewProcessDeploymentView.class)
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
