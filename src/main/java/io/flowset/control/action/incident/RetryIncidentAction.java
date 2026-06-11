/*
 * Copyright (c) Haulmont 2026. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.flowset.control.action.incident;

import com.vaadin.flow.component.Component;
import io.flowset.control.entity.incident.IncidentData;
import io.flowset.control.security.accesscontext.incident.IncidentRetryAccessContext;
import io.flowset.control.view.incidentdata.RetryExternalTaskView;
import io.flowset.control.view.incidentdata.RetryJobView;
import io.jmix.core.Messages;
import io.jmix.core.AccessManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.view.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import static io.jmix.flowui.component.UiComponentUtils.getCurrentView;

@ActionType(RetryIncidentAction.ID)
public class RetryIncidentAction extends SecuredBaseAction implements ExecutableAction {

    public static final String ID = "control_retryIncident";

    protected DialogWindows dialogWindows;

    protected IncidentData incidentData;
    protected Runnable afterSaveHandler;

    protected boolean visibleByActionUiPermission;

    public RetryIncidentAction() {
        super(ID);
    }

    public RetryIncidentAction(String id) {
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

    public void setIncidentData(IncidentData incidentData) {
        this.incidentData = incidentData;
        refreshState();
    }

    public void setAfterSaveHandler(Runnable afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    @Override
    public void execute() {
        if (incidentData.isJobFailed()) {
            dialogWindows.view(getCurrentView(), RetryJobView.class)
                    .withAfterCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
                    })
                    .withViewConfigurer(view -> view.setJobId(incidentData.getConfiguration()))
                    .build()
                    .open();
        } else if (incidentData.isExternalTaskFailed()) {
            dialogWindows.view(getCurrentView(), RetryExternalTaskView.class)
                    .withAfterCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(StandardOutcome.SAVE) && afterSaveHandler != null) {
                            afterSaveHandler.run();
                        }
                    })
                    .withViewConfigurer(view -> view.setExternalTaskId(incidentData.getConfiguration()))
                    .build()
                    .open();
        }
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
