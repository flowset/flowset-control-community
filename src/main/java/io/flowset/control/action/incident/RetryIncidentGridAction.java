/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.incident;

import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.security.accesscontext.incident.IncidentRetryAccessContext;
import io.flowset.control.view.incidentdata.RetryExternalTaskView;
import io.flowset.control.view.incidentdata.RetryJobView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(RetryIncidentGridAction.ID)
public class RetryIncidentGridAction extends ListDataComponentAction<RetryIncidentGridAction, IncidentData> {

    public static final String ID = "control_retryGridIncident";

    protected DialogWindows dialogWindows;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public RetryIncidentGridAction() {
        super(ID);
    }

    public RetryIncidentGridAction(String id) {
        super(id);
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        IncidentRetryAccessContext context = new IncidentRetryAccessContext();
        accessManager.applyRegisteredConstraints(context);

        visibleByActionUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Retry");
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        checkTarget();
        IncidentData incident = target.getSingleSelectedItem();
        if (incident == null || incident.getConfiguration() == null) {
            return;
        }
        if (incident.isExternalTaskFailed()) {
            dialogWindows.view(getCurrentView(), RetryExternalTaskView.class)
                    .withViewConfigurer(view -> view.setExternalTaskId(incident.getConfiguration()))
                    .withAfterCloseListener(afterClose -> {
                        if (afterClose.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
                    })
                    .build()
                    .open();
        } else if (incident.isJobFailed()) {
            dialogWindows.view(getCurrentView(), RetryJobView.class)
                    .withViewConfigurer(view -> view.setJobId(incident.getConfiguration()))
                    .withAfterCloseListener(afterClose -> {
                        if (afterClose.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
                    })
                    .build()
                    .open();
        }
    }

    @Override
    public void refreshState() {
        super.refreshState();

        setVisibleInternal(visibleExplicitly && visibleByActionUiPermission);
    }

    @Override
    protected boolean isApplicable() {
        IncidentData selectedItem = target.getSingleSelectedItem();
        return super.isApplicable()
                && selectedItem != null
                && selectedItem.getConfiguration() != null
                && (selectedItem.isJobFailed() || selectedItem.isExternalTaskFailed());
    }
}
