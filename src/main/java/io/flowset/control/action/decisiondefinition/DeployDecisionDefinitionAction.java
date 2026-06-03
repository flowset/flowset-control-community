/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.decisiondefinition;

import com.vaadin.flow.component.Component;
import io.flowset.control.security.accesscontext.decisiondefinition.DecisionDefinitionDeployAccessContext;
import io.flowset.control.view.decisiondeployment.DecisionDeploymentView;
import io.jmix.core.AccessManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(DeployDecisionDefinitionAction.ID)
public class DeployDecisionDefinitionAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_deployDecisionDefinition";

    protected ViewNavigators viewNavigators;

    protected boolean visibleByActionUiPermission;

    public DeployDecisionDefinitionAction() {
        super(ID);
    }

    public DeployDecisionDefinitionAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        DecisionDefinitionDeployAccessContext context = new DecisionDefinitionDeployAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }

    @Override
    public void execute() {
        viewNavigators.view(getCurrentView(), DecisionDeploymentView.class)
                .withBackwardNavigation(true)
                .navigate();
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }
}
