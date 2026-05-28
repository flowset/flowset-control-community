/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.deployment;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteParameters;
import io.flowset.control.action.ViewEntityDetailAction;
import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.view.deploymentdata.DeploymentDetailView;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import org.apache.commons.lang3.StringUtils;

import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(ViewDeploymentAction.ID)
public class ViewDeploymentAction extends ViewEntityDetailAction implements ExecutableAction {

    public static final String ID = "control_viewDeployment";

    public ViewDeploymentAction() {
        super(ID, DeploymentData.class);
    }

    public ViewDeploymentAction(String id) {
        super(id, DeploymentData.class);
    }

    public void setDeploymentId(String deploymentId) {
        setEntityId(deploymentId);
    }

    @Override
    public void execute() {
        if (StringUtils.isBlank(getEntityId())) {
            return;
        }
        viewNavigators.detailView(getCurrentView(), entityClass)
                .withViewClass(DeploymentDetailView.class)
                .withRouteParameters(new RouteParameters("id", getEntityId()))
                .withBackwardNavigation(true)
                .navigate();
    }

    @Override
    public void actionPerform(Component component) {
        execute();
    }
}
