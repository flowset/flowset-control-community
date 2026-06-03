/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.deployment;

import io.flowset.control.entity.deployment.DeploymentData;
import io.flowset.control.security.SecuritySupport;
import io.flowset.control.view.deploymentdata.BulkDeleteDeploymentView;
import io.flowset.control.view.deploymentdata.DeleteDeploymentView;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(BulkDeleteDeploymentAction.ID)
public class BulkDeleteDeploymentAction extends ListDataComponentAction<BulkDeleteDeploymentAction, DeploymentData> {

    public static final String ID = "control_bulkDeleteDeployment";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public BulkDeleteDeploymentAction() {
        super(ID);
    }

    public BulkDeleteDeploymentAction(String id) {
        super(id);
    }

    @Autowired
    protected void setSecuritySupport(SecuritySupport securitySupport) {
        visibleByActionUiPermission = securitySupport.isEntityDeletePermitted(DeploymentData.class);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Remove");
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }


    @Override
    public void execute() {
        checkTarget();

        Set<DeploymentData> selectedItems = target.getSelectedItems();
        if (selectedItems.isEmpty()) {
            return;
        }

        if (selectedItems.size() == 1) {
            String deploymentId = selectedItems.iterator().next().getId();
            dialogWindows.view(getCurrentView(), DeleteDeploymentView.class)
                    .withAfterCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
                    })
                    .withViewConfigurer(view -> view.setDeploymentId(deploymentId))
                    .build()
                    .open();
        } else {
            dialogWindows.view(getCurrentView(), BulkDeleteDeploymentView.class)
                    .withAfterCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
                    })
                    .withViewConfigurer(view -> view.setDeployments(selectedItems))
                    .build()
                    .open();
        }
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && !target.getSelectedItems().isEmpty();
    }
}
